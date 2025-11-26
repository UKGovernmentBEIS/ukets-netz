package uk.gov.netz.api.workflow.request.core.service;

import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

public interface AvailableRequestResourceTypeHandler {

	@Transactional
	Map<String, RequestCreateValidationResult> getAvailableRequestsForResource(@NotNull String resourceId, Set<String> requestTypes, AppUser appUser);

    String getResourceType();
}
