package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestTaskCancelActionDelegatorHandler<T extends RequestTaskActionPayload>
		implements RequestTaskActionHandler<T> {
	
	private final RequestTaskService requestTaskService; 
	private final RequestTaskCancelActionHandlerResolver requestTaskCancelActionHandlerResolver;
	
	@Override
	public RequestTaskPayload process(Long requestTaskId, String requestTaskActionType, AppUser appUser, T payload) {
		final RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);
		requestTaskCancelActionHandlerResolver
			.resolve(requestTask.getType().getCode())
			.cancel(requestTaskId, payload, appUser);

		return requestTask.getPayload();
	}

	@Override
	public List<String> getTypes() {
		return List.of(RequestTaskActionTypes.CANCEL_APPLICATION);
	}

}
