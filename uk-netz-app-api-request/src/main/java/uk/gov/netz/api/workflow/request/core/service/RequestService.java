package uk.gov.netz.api.workflow.request.core.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestStatuses;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;

import java.time.LocalDateTime;

@Validated
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestActionUserInfoResolver requestActionUserInfoResolver;

    /**
     * Returns request by request id.
     *
     * @param id Request id
     * @return {@link Request}
     */
    public Request findRequestById(String id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    
    public Request findRequestByIdForUpdate(String id) {
        return requestRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }


    @Transactional
    public void addActionToRequest(Request request, @Valid RequestActionPayload payload,
                                   String actionType, String submittedBy) {

        final String fullName = submittedBy != null ? requestActionUserInfoResolver.getUserFullName(submittedBy) : null;

        request.addRequestAction(
                RequestAction.builder()
                        .payload(payload)
                        .type(actionType)
                        .submitterId(submittedBy)
                        .submitter(fullName)
                        .build());
    }

    @Transactional
    public void updateRequestStatus(String requestId, String status) {
        Request request = findRequestById(requestId);

        request.setStatus(status);
    }

    @Transactional
    public void terminateRequest(String requestId, String processInstanceId, boolean shouldBeDeleted) {
        Request request = findRequestById(requestId);

        // check if the process instance that does terminate is the main process
     	// instance and not any bpmn execution sub process instance
        if (processInstanceId.equals(request.getProcessInstanceId())) {
            if (shouldBeDeleted) {
                requestRepository.delete(request);
            } else {
                closeRequest(request);
            }
        }
    }

    @Transactional
    public void paymentCompleted(final String requestId) {
        this.findRequestById(requestId).getPayload().setPaymentCompleted(true);
    }
    
    private void closeRequest(Request request) {
        if (RequestStatuses.IN_PROGRESS.equals(request.getStatus())) {
            request.setStatus(RequestStatuses.COMPLETED);
        }

        if (!request.getType().isHoldHistory()) {
            request.setPayload(null);
        }

        request.setEndDate(LocalDateTime.now());
    }
}
