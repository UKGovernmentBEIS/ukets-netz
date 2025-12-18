package uk.gov.netz.api.files.common.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileTypesProperties;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("classpath:application.properties")
class FileTypeValidatorServiceTest {
    private FileTypeValidatorService service;

    @Mock
    private FileTypesProperties fileTypesProperties;

    @Mock
    private FileTypeCustomValidator fileTypeCustomValidator;

    @BeforeEach
    void setUp() {
        service = new FileTypeValidatorService(fileTypesProperties, List.of(fileTypeCustomValidator));
    }


    @Test
    void createFileDocument_invalid_file_type() {
        String filename = "filename.lala";
        String fileContent = "some content";
        FileDTO fileDTO = FileDTO.builder()
                .fileName(filename)
                .fileSize(2)
                .fileType("application/octet-stream")
                .fileContent(fileContent.getBytes())
                .build();

        BusinessException be = assertThrows(BusinessException.class,
                () -> service.validate(fileDTO));

        Assertions.assertEquals(ErrorCode.INVALID_FILE_TYPE, be.getErrorCode());
        Assertions.assertEquals(Arrays.toString(new String[] {"application/octet-stream"}), Arrays.toString(be.getData()));
    }

    @Test
    void createFileDocument_invalid_file_type_custom_validator() {
        String filename = "filename.lala";
        String fileContent = "some content";
        FileDTO fileDTO = FileDTO.builder()
                .fileName(filename)
                .fileSize(2)
                .fileType("application/octet-stream")
                .fileContent(fileContent.getBytes())
                .build();

        when(fileTypesProperties.getAllowedMimeTypes()).thenReturn(List.of("application/octet-stream"));
        when(fileTypeCustomValidator.getApplicableMimeTypes()).thenReturn(Set.of("application/octet-stream"));
        Mockito.doThrow(new BusinessException(ErrorCode.INVALID_FILE_TYPE, fileDTO.getFileType())).when(fileTypeCustomValidator).validate(fileDTO);

        BusinessException be = assertThrows(BusinessException.class,
                () -> service.validate(fileDTO));

        Assertions.assertEquals(ErrorCode.INVALID_FILE_TYPE, be.getErrorCode());
        Assertions.assertEquals(Arrays.toString(new String[] {"application/octet-stream"}), Arrays.toString(be.getData()));
        verify(fileTypeCustomValidator, times(1)).getApplicableMimeTypes();
        verify(fileTypeCustomValidator, times(1)).validate(fileDTO);

    }

    @Test
    void createFileDocument_valid_file_type() {
        String filename = "filename";
        String fileContent = "some content";
        FileDTO fileDTO = FileDTO.builder()
                .fileName(filename)
                .fileSize(2)
                .fileType("application/msword")
                .fileContent(fileContent.getBytes())
                .build();
        when(fileTypesProperties.getAllowedMimeTypes()).thenReturn(List.of("application/msword"));
        when(fileTypeCustomValidator.getApplicableMimeTypes()).thenReturn(Set.of());

        assertDoesNotThrow(() -> service.validate(fileDTO));
    }
}