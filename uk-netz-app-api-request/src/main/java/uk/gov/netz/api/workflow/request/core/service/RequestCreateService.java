package uk.gov.netz.api.workflow.request.core.service;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.bpmn.WorkflowTypeServiceDelegator;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

@RequiredArgsConstructor
@Service
public class RequestCreateService {

    private final RequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final AccountQueryService accountQueryService;
    private final WorkflowTypeServiceDelegator workflowTypeServiceDelegator;

    /**
     * Create and persist request.
     *
     * @param requestParams the {@link RequestParams}
     * @param status        the {@link RequestStatuses}
     * @return the request created
     */
    @Transactional
    public Request createRequest(RequestParams requestParams, String requestStatus) {
        Request request = new Request();
        request.setId(requestParams.getRequestId());
		request.setType(requestTypeRepository.findByCode(requestParams.getType())
				.orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_TYPE_NOT_FOUND)));
		request.setEngine(workflowTypeServiceDelegator.getWorkflowEngineByType(requestParams.getType()));
        request.setStatus(requestStatus);
        request.setPayload(requestParams.getRequestPayload());
        request.setMetadata(requestParams.getRequestMetadata());
        if (requestParams.getCreationDate() != null) {
            request.setCreationDate(requestParams.getCreationDate());
        }
        request.setRequestResources(createRequestResources(requestParams, request));

        return requestRepository.save(request);
    }

    private List<RequestResource> createRequestResources(RequestParams requestParams, Request request) {
    	List<RequestResource> requestResources = new ArrayList<>(); 
		if (requestParams.getRequestResources() != null) {
			Long accountId = requestParams.getAccountId();
			
			requestParams.getRequestResources().forEach((resourceType, resourceId) -> {
				RequestResource requestResource = RequestResource.builder()
						.resourceType(resourceType)
						.resourceId(resourceId)
						.request(request)
						.build();
				requestResources.add(requestResource);
			});
			
			// Add competent authority if not exists in requestParams
			Optional.ofNullable(createCaResource(requestParams, request, accountId))
				.ifPresent(requestResources::add);
			
			// Add verification body id if accountId exists
			if (accountId != null) {
				Optional.ofNullable(createVbIdResource(request, accountId))
					.ifPresent(requestResources::add);
			}
		}
		return requestResources;
	}

    private RequestResource createCaResource(RequestParams requestParams, Request request, Long accountId) {
		return (requestParams.getCompetentAuthority() == null) 
				? RequestResource.builder()
					.resourceType(ResourceType.CA)
					.resourceId(accountQueryService.getAccountCa(accountId).name())
					.request(request)
					.build() 
					: null;
	}
    
	private RequestResource createVbIdResource(Request request, Long accountId) {
			Optional<Long> vbId = accountQueryService.getAccountVerificationBodyId(accountId);
		return vbId.isPresent() 
				? RequestResource.builder()
					.resourceType(ResourceType.VERIFICATION_BODY)
					.resourceId(vbId.get().toString())
					.request(request)
					.build()
		: null;
	}
}
