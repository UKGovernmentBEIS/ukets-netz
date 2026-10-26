package uk.gov.netz.api.request.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.repository.RequestFeedbackRepository;

@Service
@RequiredArgsConstructor
public class RequestFeedbackQueryService {

    private final RequestFeedbackRepository repository;

    public RequestFeedback getRequestFeedback(String requestId, String type) {
        return repository.findByRequestIdAndType(requestId, type)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}