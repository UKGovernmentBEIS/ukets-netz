package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.util.List;

@Component
@Order(200)
@Validated
@RequiredArgsConstructor
public class FileTypeValidatorService implements FileValidatorService {
    private final FileTypesProperties fileTypesProperties;
    private final List<FileTypeCustomValidator> fileTypeCustomValidators;

    @Override
    public void validate(@Valid FileDTO fileDTO) {
        if (fileTypesProperties.getAllowedMimeTypes().stream()
                .noneMatch(mimeType -> mimeType.equals(fileDTO.getFileType()))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, fileDTO.getFileType());
        }

        fileTypeCustomValidators.stream()
                .filter(fileTypeCustomValidator -> fileTypeCustomValidator.getApplicableMimeTypes().contains(fileDTO.getFileType()))
                .forEach(fileTypeCustomValidator -> fileTypeCustomValidator.validate(fileDTO));
    }
}
