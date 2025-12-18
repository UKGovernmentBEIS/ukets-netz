package uk.gov.netz.api.files.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class FileScanValidatorTest {

    @InjectMocks
    private FileScanValidatorService fileScanValidator;

    @Mock
    private FileScanService fileScanService;

    @Test
    void validate() {
        FileDTO fileDTO = FileDTO.builder()
            .fileName("name")
            .fileSize(5)
            .fileType("application/pdf")
            .fileContent(new byte[]{})
            .build();

        doThrow(new BusinessException(ErrorCode.INFECTED_STREAM))
            .when(fileScanService).scan(any(InputStream.class));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileScanValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INFECTED_STREAM);
    }
}