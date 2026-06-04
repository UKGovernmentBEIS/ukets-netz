package uk.gov.netz.api.files.common.service.filecontentvalidators;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextContentValidatorServiceTest {

    private final TextContentValidatorService validator = new TextContentValidatorService("(?m)^[=@+-]|[,\\n\\r][=@+-]");

    @Test
    void shouldRejectFormulaInjection() {
        String csv = "Name,Amount\nJohn,=SUM(A1:A2)";
        FileDTO dto = new FileDTO();
        dto.setFileContent(csv.getBytes(StandardCharsets.UTF_8));

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }

    @Test
    void shouldRejectNullBytes() {
        byte[] content = { 'a', 'b', 0, 'c' };
        FileDTO dto = new FileDTO();
        dto.setFileContent(content);

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }

    @Test
    void shouldPassCleanText() {
        FileDTO dto = new FileDTO();
        dto.setFileContent("Normal text without symbols".getBytes());

        assertDoesNotThrow(() -> validator.validate(dto));
    }
}