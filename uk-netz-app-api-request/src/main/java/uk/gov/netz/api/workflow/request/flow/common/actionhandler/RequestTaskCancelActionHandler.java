package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

public interface RequestTaskCancelActionHandler<T extends RequestTaskActionPayload> {
	
	@Transactional
	void cancel(Long requestTaskId, T payload, AppUser appUser);
	
	List<String> getRequestTaskTypes();

}
