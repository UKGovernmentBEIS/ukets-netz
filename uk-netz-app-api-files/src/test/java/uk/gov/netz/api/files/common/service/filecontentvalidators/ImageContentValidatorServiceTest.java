package uk.gov.netz.api.files.common.service.filecontentvalidators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class ImageContentValidatorServiceTest {

    private ImageContentValidatorService validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ImageContentValidatorService();
        Field maxWidthField = ImageContentValidatorService.class.getDeclaredField("maxWidth");
        maxWidthField.setAccessible(true);
        maxWidthField.setInt(validator, 10000);
        Field maxHeightField = ImageContentValidatorService.class.getDeclaredField("maxHeight");
        maxHeightField.setAccessible(true);
        maxHeightField.setInt(validator, 10000);
    }

    @Test
    void shouldPassValidImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", bos);
        } catch (IOException e) { fail(); }

        FileDTO dto = new FileDTO();
        dto.setFileContent(bos.toByteArray());

        assertDoesNotThrow(() -> validator.validate(dto));
    }

    @Test
    void shouldRejectLargePixels() {
        BufferedImage img = new BufferedImage(11000, 11000, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", bos);
        } catch (IOException e) { fail(); }

        FileDTO dto = new FileDTO();
        dto.setFileContent(bos.toByteArray());

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }

    @Test
    void shouldRejectGarbageData() {
        FileDTO dto = new FileDTO();
        dto.setFileContent("this is not an image".getBytes());

        assertThrows(SecurityException.class, () -> validator.validate(dto));
    }
}