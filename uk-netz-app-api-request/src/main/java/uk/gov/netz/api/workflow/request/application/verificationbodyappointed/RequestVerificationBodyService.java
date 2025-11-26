package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
class RequestVerificationBodyService {

    private final RequestRepository requestRepository;
    private final WorkflowService workflowService;
    private final RequestService requestService;

    @Transactional
    public void appointVerificationBodyToRequestsOfAccount(Long verificationBodyId, Long accountId) {
        List<Request> requests = requestRepository.findAllByAccountId(accountId);

        List<Long> existingTaskIds = requests.stream()
                .map(request -> request.getRequestTasks()
                		.stream()
                		.filter(requestTask -> requestTask.getType().getCode().endsWith(RequestTaskTypes.APPLICATION_VERIFICATION_SUBMIT))
                		.collect(Collectors.toList())
                		)
                .flatMap(List::stream)
                .map(RequestTask::getId)
                .collect(Collectors.toList());

        if (!existingTaskIds.isEmpty()) {
            //verifier_related_request_tasks_exist_for_account
            throw new BusinessException(ErrorCode.VERIFICATION_RELATED_REQUEST_TASKS_EXIST_FOR_ACCOUNT, existingTaskIds.toArray());
        }

        updateRequestsVbAndRemoveVerifierAssignee(requests, verificationBodyId);
    }

    @Transactional
    public void unappointVerificationBodyFromRequestsOfAccounts(Set<Long> accountIds) {
        List<Request> requests = requestRepository.findAllByAccountIdIn(accountIds);
        requests.forEach(request -> {
            List<RequestTask> tasks = request.getRequestTasks().stream()
                    .filter(requestTask -> requestTask.getType().getCode().endsWith(RequestTaskTypes.APPLICATION_VERIFICATION_SUBMIT))
                    .collect(Collectors.toList());
            tasks.forEach(task -> {
                workflowService.sendEvent(request.getId(), BpmnProcessConstants.VERIFICATION_BODY_STATE_CHANGED, Map.of());
                requestService.addActionToRequest(
                        request,
                        null,
                        RequestActionTypes.VERIFICATION_STATEMENT_CANCELLED,
                        null
                );
            });
            request.getRequestResources().remove(getVbResource(request));
            removeVerifierAssignee(request);
        });
    }

    private void updateRequestsVbAndRemoveVerifierAssignee(List<Request> requests, Long newVerificationBodyId) {
        requests.forEach(request -> {
        	
        	RequestResource vbResource = getVbResource(request);
        	// Create new or update existing resource
        	if (vbResource == null) {
        		vbResource = RequestResource.builder()
    					.resourceType(ResourceType.VERIFICATION_BODY)
    					.resourceId(newVerificationBodyId.toString())
    					.request(request)
    					.build();
        		request.getRequestResources().add(vbResource);
        	} else {
        		vbResource.setResourceId(newVerificationBodyId.toString());
        	}
    
            removeVerifierAssignee(request);
        });
    }

	private RequestResource getVbResource(Request request) {
		return request.getRequestResources().stream()
		.filter(resource -> ResourceType.VERIFICATION_BODY.equals(resource.getResourceType()))
		.findFirst()
		.orElse(null);
	}
	
	private void removeVerifierAssignee(Request request) {
		RequestPayload requestPayload = request.getPayload();
		if (requestPayload != null) {
		    requestPayload.setVerifierAssignee(null);
		}
	}
}
