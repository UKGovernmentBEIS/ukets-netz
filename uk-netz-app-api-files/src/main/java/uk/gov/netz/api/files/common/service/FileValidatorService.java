package uk.gov.netz.api.files.common.service;

import jakarta.validation.Valid;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

public interface FileValidatorService {

    void validate(@Valid FileDTO fileDTO);
}
