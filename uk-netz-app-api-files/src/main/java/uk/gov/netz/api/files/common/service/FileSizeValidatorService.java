package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileConstants;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

@Component
@Order(100)
@Validated
public class FileSizeValidatorService implements FileValidatorService {

    @Override
    public void validate(@Valid FileDTO fileDTO) {
        long fileSize = fileDTO.getFileSize();

        if (fileSize <= FileConstants.MIN_FILE_SIZE) {
            throw new BusinessException(ErrorCode.MIN_FILE_SIZE_ERROR, fileSize);
        }

        long maxAllowedSize = FileConstants.SYSTEM_USER.equals(fileDTO.getCreatedBy())
                ? FileConstants.MAX_SYSTEM_GENERATED_FILE_SIZE
                : FileConstants.MAX_FILE_SIZE;

        if (fileSize >= maxAllowedSize) {
            throw new BusinessException(ErrorCode.MAX_FILE_SIZE_ERROR, fileSize);
        }
    }
}
