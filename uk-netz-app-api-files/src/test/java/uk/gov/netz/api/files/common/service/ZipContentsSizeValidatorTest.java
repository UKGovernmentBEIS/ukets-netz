package uk.gov.netz.api.files.common.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

@ExtendWith(MockitoExtension.class)
class ZipContentsSizeValidatorTest {
	
	@InjectMocks
    private ZipContentsSizeValidator cut;

    @Mock
    private FileTypesProperties fileTypesProperties;

    @Test
    void validate_size_empty_error() {
        FileDTO fileDTO = FileDTO.builder()
                .fileName("test.zip")
                .fileSize(20)
                .fileContent("".getBytes())
                .build();

        BusinessException be = assertThrows(BusinessException.class,
                () -> cut.validate(fileDTO));

        assertEquals(ErrorCode.ZIP_FILE_EMPTY, be.getErrorCode());
        verifyNoInteractions(fileTypesProperties);
    }

    @Test
    void validate_size_error() throws IOException {
    	long maxSizeAllowedMb = 60; // 60mb
    	Path filePath = Paths.get("src", "test", "resources", "files", "test_zipbomb.zip");
    	
    	FileTypesProperties.Zip zip = new FileTypesProperties.Zip();
    	zip.setExtractedMaxSizeMb(maxSizeAllowedMb);
    	
    	when(fileTypesProperties.getZip()).thenReturn(zip);
    	
        FileDTO fileDTO = FileDTO.builder()
                .fileName(filePath.getFileName().toString())
                .fileSize(Files.size(filePath))
                .fileContent(Files.readAllBytes(filePath))
                .build();

        BusinessException be = assertThrows(BusinessException.class,
                () -> cut.validate(fileDTO));

        assertEquals(ErrorCode.ZIP_FILE_EXTRACTED_MAX_SIZE_ERROR, be.getErrorCode());
        verify(fileTypesProperties, times(1)).getZip();
    }
    
    @Test
    void validate_valid() throws IOException {
    	long maxSizeAllowedMb = 60; // 60mb
    	Path filePath = Paths.get("src", "test", "resources", "files", "test_valid.zip");
    	
    	FileTypesProperties.Zip zip = new FileTypesProperties.Zip();
    	zip.setExtractedMaxSizeMb(maxSizeAllowedMb);
    	
    	when(fileTypesProperties.getZip()).thenReturn(zip);
    	
        FileDTO fileDTO = FileDTO.builder()
                .fileName(filePath.getFileName().toString())
                .fileSize(Files.size(filePath))
                .fileContent(Files.readAllBytes(filePath))
                .build();

        assertDoesNotThrow(() -> cut.validate(fileDTO));
        verify(fileTypesProperties, times(1)).getZip();
    }
    
}
