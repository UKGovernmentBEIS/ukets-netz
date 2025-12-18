package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

import java.util.Set;

public interface FileTypeCustomValidator {
    void validate(@Valid FileDTO fileDTO);
    Set<String> getApplicableMimeTypes();
}
