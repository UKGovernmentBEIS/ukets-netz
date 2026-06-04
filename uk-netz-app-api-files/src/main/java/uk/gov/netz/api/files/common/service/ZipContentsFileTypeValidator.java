package uk.gov.netz.api.files.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileType;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.service.filecontentvalidators.FileContentValidatorService;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

@Component
@Order(200)
@RequiredArgsConstructor
@Log4j2
public class ZipContentsFileTypeValidator implements FileTypeCustomValidator {
    private final FileTypesProperties fileTypesProperties;
    private final List<FileContentValidatorService> fileContentValidators;

    @Value("${file.content.validators.enabled:}")
    private List<String> enabledValidators = new ArrayList<>();

    @Override
    public void validate(FileDTO fileDTO) {
        try {
            ZipFileExtractor.consumeZip(fileDTO.getFileContent(), this::fileTypeValidator);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER);
        }
    }

    private void fileTypeValidator(ZipEntry entry, InputStream is) {
        try {
            byte[] entryContent = is.readAllBytes();

            String mimeType = MimeTypeUtils.detect(new ByteArrayInputStream(entryContent), entry.getName());

            if (!fileTypesProperties.getZip().getAllowedMimeTypes().contains(mimeType)) {
                log.warn("Unauthorized file type [{}] found in ZIP entry [{}]", mimeType, entry.getName());
                throw new BusinessException(ErrorCode.ZIP_FILE_CONTAINS_INVALID_FILE_TYPE);
            }

            FileDTO entryDto = new FileDTO();
            entryDto.setFileContent(entryContent);
            entryDto.setFileType(mimeType);

            fileContentValidators.stream()
                    .filter(validator -> enabledValidators.contains(validator.getName()))
                    .filter(validator -> validator.supports(mimeType))
                    .forEach(validator -> validator.validate(entryDto));

        } catch (IOException e) {
            log.error("Failed to read ZIP entry content: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER);
        }
    }


    @Override
    public Set<String> getApplicableMimeTypes() {
        return FileType.ZIP.getMimeTypes();
    }
}
