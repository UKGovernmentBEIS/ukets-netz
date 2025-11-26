package uk.gov.netz.api.workflow.request.core.assignment.requestassign.release;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;

@Service
@RequiredArgsConstructor
public class RequestReleaseService {

    private final AuthorizationRulesQueryService authorizationRulesQueryService;
    private final List<UserRoleTypeRequestReleaseService> userRoleTypeRequestReleaseServices;
    
    @Transactional
    public void releaseRequest(RequestTask requestTask) {
        if (requestTask.getType().isSupporting()) {
            return;
        }
        
        final Request request = requestTask.getRequest();
        final String requestTaskAssignee = requestTask.getAssignee();

        final String requestTaskRoleType = authorizationRulesQueryService
            .findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, requestTask.getType().getCode())
            .orElse(null);
        
        if (requestTaskRoleType == null) {
        	return;
        }

    	userRoleTypeRequestReleaseServices
		.stream()
		.filter(service -> service.getRoleType().equals(requestTaskRoleType)).findAny()
			.orElseThrow(() -> new UnsupportedOperationException(
						String.format("User with role type %s not related with request assignment", requestTaskRoleType)))
		.release(request, requestTaskAssignee);
    }

}
