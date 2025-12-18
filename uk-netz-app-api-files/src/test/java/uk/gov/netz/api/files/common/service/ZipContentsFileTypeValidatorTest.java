package uk.gov.netz.api.files.common.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;

@ExtendWith(MockitoExtension.class)
public class ZipContentsFileTypeValidatorTest {

	@InjectMocks
    private ZipContentsFileTypeValidator cut;

    @Mock
    private FileTypesProperties fileTypesProperties;

    @Test
    void validateFile_valid() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "test_valid.zip");
        FileDTO file = createFile(sampleFilePath);
        
        FileTypesProperties.Zip zip = new FileTypesProperties.Zip();
		zip.setAllowedMimeTypes(List.of("text/plain"));

        when(fileTypesProperties.getZip()).thenReturn(zip);

        assertDoesNotThrow(() -> cut.validate(file));
    }
    
    @Test
    void validateFile_zipbomb_valid() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "test_zipbomb.zip");
        FileDTO file = createFile(sampleFilePath);
        
        FileTypesProperties.Zip zip = new FileTypesProperties.Zip();
		zip.setAllowedMimeTypes(List.of("text/plain"));

        when(fileTypesProperties.getZip()).thenReturn(zip);

        assertDoesNotThrow(() -> cut.validate(file));
    }

    @Test
    void validateFile_invalid() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "test_invalid.zip");
        FileDTO file = createFile(sampleFilePath);

        FileTypesProperties.Zip zip = new FileTypesProperties.Zip();
		zip.setAllowedMimeTypes(List.of("text/plain"));
        
		when(fileTypesProperties.getZip()).thenReturn(zip);

        BusinessException be = assertThrows(BusinessException.class,
                () -> cut.validate(file));

        Assertions.assertEquals(ErrorCode.ZIP_FILE_CONTAINS_INVALID_FILE_TYPE, be.getErrorCode());
        Assertions.assertArrayEquals(List.of("application/zip", "signature_valid.dib").toArray(), be.getData());
    }

    private FileDTO createFile(Path sampleFilePath) throws IOException {
        byte[] bytes = Files.readAllBytes(sampleFilePath);
        return FileDTO.builder()
                .fileContent(bytes)
                .fileName(sampleFilePath.getFileName().toString())
                .fileSize(sampleFilePath.toFile().length())
                .fileType(MimeTypeUtils.detect(bytes, sampleFilePath.getFileName().toString()))
                .build();
    }
    
}
