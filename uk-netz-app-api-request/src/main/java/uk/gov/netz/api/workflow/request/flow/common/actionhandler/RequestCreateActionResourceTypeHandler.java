package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;

public interface RequestCreateActionResourceTypeHandler<T extends RequestCreateActionPayload> {

    @Transactional
    String process(@NotNull String resourceId, String requestType, T payload, AppUser appUser);

    String getResourceType();

}
