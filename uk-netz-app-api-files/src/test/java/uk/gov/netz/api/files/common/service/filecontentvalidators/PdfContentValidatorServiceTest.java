package uk.gov.netz.api.files.common.service.filecontentvalidators;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfContentValidatorServiceTest {

    private PdfContentValidatorService validator;

    @BeforeEach
    void setUp() {
        validator = new PdfContentValidatorService();
        ReflectionTestUtils.setField(validator, "maxPages", 1000);
    }


    @Test
    void shouldPassValidPdf() throws IOException {

        byte[] pdfContent;
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            document.save(bos);
            pdfContent = bos.toByteArray();
        }

        FileDTO dto = new FileDTO();
        dto.setFileContent(pdfContent);
        dto.setFileType("application/pdf");

        assertDoesNotThrow(() -> validator.validate(dto));
    }

    @Test
    void shouldRejectInvalidPdfStructure() {
        FileDTO dto = new FileDTO();
        dto.setFileContent("%PDF-1.5    ".getBytes());

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }

    @Test
    void shouldRejectEmptyFile() {
        FileDTO dto = new FileDTO();
        dto.setFileContent(new byte[0]);

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }
}