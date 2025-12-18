package uk.gov.netz.api.files.common.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSizeValidatorTest {

    private final FileSizeValidatorService fileSizeValidator = new FileSizeValidatorService();

    @Test
    void validate_max_size_reached() {
        FileDTO fileDTO = createFileDTO(30000000);
        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileSizeValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MAX_FILE_SIZE_ERROR);
        Assertions.assertEquals(Arrays.toString(new long[] {30000000}), Arrays.toString(exception.getData()));
    }

    @Test
    void validate_zero_size() {
        FileDTO fileDTO = createFileDTO(0);
        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileSizeValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MIN_FILE_SIZE_ERROR);
        Assertions.assertEquals(Arrays.toString(new long[] {0}), Arrays.toString(exception.getData()));
    }

    private FileDTO createFileDTO(long fileSize) {
        return FileDTO.builder()
            .fileName("name")
            .fileSize(fileSize)
            .fileType("application/pdf")
            .fileContent(new byte[]{})
            .build();
    }
}