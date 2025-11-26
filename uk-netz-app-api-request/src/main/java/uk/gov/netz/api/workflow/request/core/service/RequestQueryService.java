package uk.gov.netz.api.workflow.request.core.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.taskview.RequestInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayloadCascadable;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestStatuses;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsDTO;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsSearchResults;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestSearchCriteria;
import uk.gov.netz.api.workflow.request.core.repository.RequestDetailsRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Validated
@Service
@RequiredArgsConstructor
public class RequestQueryService {

    private final RequestRepository requestRepository;
    private final RequestDetailsRepository requestDetailsRepository;
    
    @Transactional(readOnly = true)
    public List<Request> findInProgressRequestsByAccount(Long accountId) {
        return requestRepository.findByAccountIdAndStatus(accountId, RequestStatuses.IN_PROGRESS);
    }
    
    @Transactional(readOnly = true)
    public List<Request> findInProgressRequestsByResource(Long resourceId, String resourceType) {
        return requestRepository.findByResourceAndStatus(resourceId, resourceType, RequestStatuses.IN_PROGRESS);
    }
    
    public Request findByProcessInstanceId(String processInstanceId) {
    	return requestRepository.findByProcessInstanceId(processInstanceId);
    }

    public boolean existsRequestById(String requestId) {
        return requestRepository.existsById(requestId);
    }

    public boolean existsRequestByAccountAndType(Long accountId, String requestType) {
        return requestRepository.existsByAccountIdAndType(accountId, requestType);
    }
    
    public boolean existByRequestTypeAndRequestStatusAndCompetentAuthority(String type, String status, CompetentAuthorityEnum competentAuthority) {
        return requestRepository.existsByTypeAndStatusAndCompetentAuthority(type, status, competentAuthority);
    }

    public RequestDetailsSearchResults findRequestDetailsBySearchCriteria(@Valid RequestSearchCriteria criteria) {
        return requestDetailsRepository.findRequestDetailsBySearchCriteria(criteria);
    }

    public RequestDetailsDTO findRequestDetailsById(String requestId) {
        return requestDetailsRepository.findRequestDetailsById(requestId)
                    .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND, requestId));
    }
    
    public List<Request> getRelatedRequests(final List<Request> requests) {

        final Set<String> relatedRequestIds = requests.stream()
            .filter(request -> request.getType().isCascadable())
            .map(Request::getPayload)
            .map(RequestPayloadCascadable.class::cast)
            .map(RequestPayloadCascadable::getRelatedRequestId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return requestRepository.findByIdInAndStatus(relatedRequestIds, RequestStatuses.IN_PROGRESS);
    }

    public List<Request> findRequestsByRequestTypeAndResourceTypeAndResourceId(String requestType, String resourceType, String resourceId) {
        return requestRepository.findByRequestTypeAndResourceTypeAndResourceId(requestType, resourceType, resourceId);
    }

    public List<RequestInfoDTO> findByResourceTypeAndResourceIdAndTypeNotIn(List<String> excludedRequestTypes, String resourceType, String resourceId) {
        return requestRepository.findByResourceTypeAndResourceIdAndTypeNotIn(excludedRequestTypes, resourceType, resourceId);
    }
}
