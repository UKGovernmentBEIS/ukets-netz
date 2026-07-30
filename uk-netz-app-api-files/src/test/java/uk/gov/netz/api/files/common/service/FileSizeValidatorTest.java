package uk.gov.netz.api.files.common.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileConstants;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSizeValidatorTest {

    private final FileSizeValidatorService fileSizeValidator = new FileSizeValidatorService();

    @Test
    void validate_max_size_reached_for_user_upload() {
        FileDTO fileDTO = createFileDTO(FileConstants.MAX_FILE_SIZE, "user");
        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileSizeValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MAX_FILE_SIZE_ERROR);
        Assertions.assertEquals(Arrays.toString(new long[] {FileConstants.MAX_FILE_SIZE}),
            Arrays.toString(exception.getData()));
    }

    @Test
    void validate_allows_user_upload_below_max() {
        FileDTO fileDTO = createFileDTO(FileConstants.MAX_FILE_SIZE - 1, "user");
        assertDoesNotThrow(() -> fileSizeValidator.validate(fileDTO));
    }

    @Test
    void validate_max_size_reached_for_system_generated() {
        FileDTO fileDTO = createFileDTO(FileConstants.MAX_SYSTEM_GENERATED_FILE_SIZE, FileConstants.SYSTEM_USER);
        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileSizeValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MAX_FILE_SIZE_ERROR);
        Assertions.assertEquals(Arrays.toString(new long[] {FileConstants.MAX_SYSTEM_GENERATED_FILE_SIZE}),
            Arrays.toString(exception.getData()));
    }

    @Test
    void validate_allows_system_generated_above_user_max_but_below_system_max() {
        FileDTO fileDTO = createFileDTO(FileConstants.MAX_FILE_SIZE + 1, FileConstants.SYSTEM_USER);
        assertDoesNotThrow(() -> fileSizeValidator.validate(fileDTO));
    }

    @Test
    void validate_zero_size() {
        FileDTO fileDTO = createFileDTO(0, "user");
        BusinessException exception = assertThrows(BusinessException.class, () ->
            fileSizeValidator.validate(fileDTO));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MIN_FILE_SIZE_ERROR);
        Assertions.assertEquals(Arrays.toString(new long[] {0}), Arrays.toString(exception.getData()));
    }

    private FileDTO createFileDTO(long fileSize, String createdBy) {
        return FileDTO.builder()
            .fileName("name")
            .fileSize(fileSize)
            .fileType("application/pdf")
            .fileContent(new byte[]{})
            .createdBy(createdBy)
            .build();
    }
}
