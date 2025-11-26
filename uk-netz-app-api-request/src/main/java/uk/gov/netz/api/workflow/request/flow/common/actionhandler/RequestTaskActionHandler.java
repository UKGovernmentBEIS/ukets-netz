package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;

import java.util.List;

public interface RequestTaskActionHandler<T extends RequestTaskActionPayload> {

    @Transactional
    RequestTaskPayload process(Long requestTaskId, String requestTaskActionType, AppUser appUser, T payload);
    
    List<String> getTypes();
}
