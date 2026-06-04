package uk.gov.netz.api.files.common.service.filecontentvalidators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@Order(300)
@Validated
@RequiredArgsConstructor
public class ImageContentValidatorService implements FileContentValidatorService {

    @Value("${file.validators.image.max-width:10000}")
    private int maxWidth;

    @Value("${file.validators.image.max-height:10000}")
    private int maxHeight;

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;

        return Set.of(
                "image/jpg",
                "image/jpeg",
                "image/png",
                "image/tiff",
                "image/bmp",
                "image/x-ms-bmp"
        ).contains(mimeType.toLowerCase());
    }

    @Override
    public void validate(FileDTO fileDTO) {
        ImageReader reader = null;
        try (InputStream is = new ByteArrayInputStream(fileDTO.getFileContent());
             ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(fileDTO.getFileContent()))) {

            reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream);
            if (reader.getWidth(0) > maxWidth ||
                    reader.getHeight(0) > maxHeight)
                throw new SecurityException("Invalid image dimensions: " + reader.getWidth(0)
                        + "x" + reader.getHeight(0));

            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) throw new SecurityException("Unable to read image data");

            ByteArrayOutputStream cleanStream = new ByteArrayOutputStream();
            if (!ImageIO.write(originalImage, reader.getFormatName(), cleanStream))
                throw new SecurityException("Failed to rewrite image");
            fileDTO.setFileContent(cleanStream.toByteArray());

        } catch (IOException | NoSuchElementException e) {
            throw new SecurityException("File content validation failed.", e);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
}
