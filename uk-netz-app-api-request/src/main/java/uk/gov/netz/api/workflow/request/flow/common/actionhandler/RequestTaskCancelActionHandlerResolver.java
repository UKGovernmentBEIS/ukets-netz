package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

import java.util.List;

@Component
@AllArgsConstructor
public class RequestTaskCancelActionHandlerResolver {
    
    private final List<RequestTaskCancelActionHandler<? extends RequestTaskActionPayload>> handlers;

    public RequestTaskCancelActionHandler resolve(String requestTaskType) {
        return handlers.stream()
				.filter(h -> h.getRequestTaskTypes().contains(requestTaskType)
						|| h.getRequestTaskTypes().stream().anyMatch(requestTaskType::endsWith))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
