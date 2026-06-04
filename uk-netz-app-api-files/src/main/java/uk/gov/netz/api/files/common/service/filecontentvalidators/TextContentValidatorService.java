package uk.gov.netz.api.files.common.service.filecontentvalidators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Order(300)
@Validated
@RequiredArgsConstructor
public class TextContentValidatorService implements FileContentValidatorService{

    @Value("${file.validator.csv.dangerous-pattern}")
    private final String dangerousPatternString;

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;

        return Set.of(
                "text/plain",
                "text/csv"
        ).contains(mimeType.toLowerCase());
    }

    @Override
    public void validate(FileDTO fileDTO) {
        String content = new String(fileDTO.getFileContent(), StandardCharsets.UTF_8);

        Pattern dangerousPattern = Pattern.compile(dangerousPatternString);

        if (dangerousPattern.matcher(content).find()) {
            throw new SecurityException("Security Risk: CSV/Text contains potential formula injection characters.");
        }
        if (content.contains("\0")) {
            throw new SecurityException("Invalid content: Null bytes detected.");
        }
    }
}
