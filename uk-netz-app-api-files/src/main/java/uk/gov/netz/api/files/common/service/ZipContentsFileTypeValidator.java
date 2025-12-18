package uk.gov.netz.api.files.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileType;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;

@Component
@Order(200)
@RequiredArgsConstructor
@Log4j2
public class ZipContentsFileTypeValidator implements FileTypeCustomValidator {
    private final FileTypesProperties fileTypesProperties;

    @Override
    public void validate(FileDTO fileDTO) {
        try {
            ZipFileExtractor.consumeZip(fileDTO.getFileContent(), (entry, is) -> fileTypeValidator(fileDTO, entry, is));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER);
        }
    }
    
    private void  fileTypeValidator(FileDTO zipFileDTO, ZipEntry entry, InputStream is) {
		try(InputStream fis = is) {
			String mimeType = MimeTypeUtils.detect(fis, entry.getName());
			if (!fileTypesProperties.getZip().getAllowedMimeTypes().contains(mimeType)) {
				throw new BusinessException(ErrorCode.ZIP_FILE_CONTAINS_INVALID_FILE_TYPE, zipFileDTO.getFileType(),
						entry.getName());
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		    throw new BusinessException(ErrorCode.INTERNAL_SERVER);
		}
	}

    @Override
    public Set<String> getApplicableMimeTypes() {
        return FileType.ZIP.getMimeTypes();
    }
}
