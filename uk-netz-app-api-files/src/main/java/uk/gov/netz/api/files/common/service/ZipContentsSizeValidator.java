package uk.gov.netz.api.files.common.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileType;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

@Log4j2
@Order(100)
@Component
@RequiredArgsConstructor
public class ZipContentsSizeValidator implements FileTypeCustomValidator {
	
	private final FileTypesProperties fileTypesProperties;

    @Override
    public void validate(FileDTO fileDTO) {
        try {
			long totalSize = calculateTotalSize(fileDTO.getFileContent());
			if(totalSize == 0) {
				throw new BusinessException(ErrorCode.ZIP_FILE_EMPTY, fileDTO.getFileName());
			}
			if(totalSize > fileTypesProperties.getZip().getExtractedMaxSizeInBytes()) {
				throw new BusinessException(ErrorCode.ZIP_FILE_EXTRACTED_MAX_SIZE_ERROR, fileDTO.getFileName());
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new BusinessException(ErrorCode.INTERNAL_SERVER);
		}
    }

	private long calculateTotalSize(byte[] zipBytes) throws IOException {
		long totalSize = 0;

		try (ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
			ZipEntry entry;

			while ((entry = zipFile.getNextEntry()) != null) {
				long size = entry.getSize();
				totalSize += size;
				zipFile.closeEntry();
			}
		}

		return totalSize;
	}

    @Override
    public Set<String> getApplicableMimeTypes() {
        return FileType.ZIP.getMimeTypes();
    }
}
