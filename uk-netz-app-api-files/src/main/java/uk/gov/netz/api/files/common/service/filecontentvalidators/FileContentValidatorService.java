package uk.gov.netz.api.files.common.service.filecontentvalidators;

import org.springframework.util.ClassUtils;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

public interface FileContentValidatorService {

    boolean supports(String mimeType);
    void validate(FileDTO fileDTO);

    default String getName() {
        return ClassUtils.getUserClass(this).getSimpleName();
    }

}
