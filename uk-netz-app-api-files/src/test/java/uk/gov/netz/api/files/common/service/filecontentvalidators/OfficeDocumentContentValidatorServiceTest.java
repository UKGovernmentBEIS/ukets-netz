package uk.gov.netz.api.files.common.service.filecontentvalidators;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfficeDocumentContentValidatorServiceTest {

    private OfficeDocumentContentValidatorService validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new OfficeDocumentContentValidatorService();
        Field field = OfficeDocumentContentValidatorService.class.getDeclaredField("macrosValidationEnabled");
        field.setAccessible(true);
        field.setBoolean(validator, true);
    }

    @Test
    void shouldPassValidOOXMLDocument() throws IOException {

        byte[] content;
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("Safe Content");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            content = bos.toByteArray();
        }

        FileDTO dto = new FileDTO();
        dto.setFileContent(content);

        assertDoesNotThrow(() -> validator.validate(dto));
    }

    @Test
    void shouldRejectOOXMLWithMacros() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("Content");
            OPCPackage pkg = doc.getPackage();
            PackagePartName partName = PackagingURIHelper.createPartName("/word/vbaProject.bin");
            pkg.createPart(partName, "application/vnd.ms-office.vbaProject");
            doc.write(bos);
        }

        FileDTO dto = new FileDTO();
        dto.setFileContent(bos.toByteArray());

        SecurityException exception = assertThrows(SecurityException.class, () -> validator.validate(dto));
        assertTrue(exception.getMessage().contains("Macros detected"));
    }

    @Test
    void shouldRejectLegacyOLE2WithMacros() throws IOException {

        byte[] content;
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            fs.createDocument(new ByteArrayInputStream(new byte[0]), "_VBA_PROJECT_CUR");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fs.writeFilesystem(bos);
            content = bos.toByteArray();
        }

        FileDTO dto = new FileDTO();
        dto.setFileContent(content);

        SecurityException exception = assertThrows(SecurityException.class, () -> validator.validate(dto));
        assertTrue(exception.getMessage().contains("Macros detected"));
    }

    @Test
    void shouldPassLegacyOLE2WithoutMacros() throws IOException {
        byte[] content;
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            fs.createDocument(new ByteArrayInputStream("Hello".getBytes()), "WordDocument");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fs.writeFilesystem(bos);
            content = bos.toByteArray();
        }

        FileDTO dto = new FileDTO();
        dto.setFileContent(content);

        assertDoesNotThrow(() -> validator.validate(dto));
    }

    @Test
    void shouldRejectNonOfficeFile() {
        FileDTO dto = new FileDTO();
        dto.setFileContent("This is just a text file".getBytes());

        SecurityException exception = assertThrows(SecurityException.class,
                () -> validator.validate(dto));

        assertTrue(exception.getMessage().contains("Invalid Office document structure"));
    }
}