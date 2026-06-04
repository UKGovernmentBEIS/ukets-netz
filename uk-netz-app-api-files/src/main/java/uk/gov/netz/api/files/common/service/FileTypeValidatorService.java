package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.service.filecontentvalidators.FileContentValidatorService;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(200)
@Validated
@RequiredArgsConstructor
public class FileTypeValidatorService implements FileValidatorService {

    private final FileTypesProperties fileTypesProperties;
    private final List<FileTypeCustomValidator> fileTypeCustomValidators;
    private final List<FileContentValidatorService> fileContentValidators;

    @Value("${file.content.validators.enabled:}")
    private List<String> enabledValidators = new ArrayList<>();

    @Override
    public void validate(@Valid FileDTO fileDTO) {
        if (fileTypesProperties.getAllowedMimeTypes().stream()
                .noneMatch(mimeType -> mimeType.equals(fileDTO.getFileType()))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, fileDTO.getFileType());
        }

        fileTypeCustomValidators.stream()
                .filter(fileTypeCustomValidator -> fileTypeCustomValidator.getApplicableMimeTypes().contains(fileDTO.getFileType()))
                .forEach(fileTypeCustomValidator -> fileTypeCustomValidator.validate(fileDTO));

        fileContentValidators.stream()
                .filter(validator -> enabledValidators.contains(validator.getName()))
                .filter(validator -> validator.supports(fileDTO.getFileType()))
                .forEach(validator -> validator.validate(fileDTO));
    }
}
