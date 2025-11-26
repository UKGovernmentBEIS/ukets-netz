package uk.gov.netz.api.workflow.request.core.validation;

import java.util.Set;

import jakarta.validation.Valid;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

public interface RequestTaskActionFileValidator {

	void validate(@Valid FileDTO fileDTO);
	
	Set<String> getRequestTaskActionTypes();
	
}
