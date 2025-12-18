package uk.gov.netz.api.files.common.utils;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import uk.gov.netz.api.common.domain.ResourceFile;

import java.io.IOException;

@UtilityClass
public class ResourceFileUtils {

    public ResourceFile getResourceFile(String filepath) throws IOException {
        byte[] content = new ClassPathResource(filepath).getInputStream().readAllBytes();

        return ResourceFile.builder()
            .fileContent(content)
            .fileSize(content.length)
            .fileType(MimeTypeUtils.detect(content, filepath))
            .build();
    }
}
