package uk.gov.netz.api.request.feedback.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackPayload;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackStatus;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackType;
import uk.gov.netz.api.request.feedback.utils.RequestFeedbackUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestFeedbackValidator {

    private final DateService dateService;


    public void validateNotExpired(LocalDateTime createdOn) {
        boolean expired = RequestFeedbackUtils.hasExpired(dateService.getLocalDateTime(), createdOn);
        if (expired) {
            throw new BusinessException(ErrorCode.REQUEST_FEEDBACK_EXPIRED);
        }
    }

    public void validateStatus(RequestFeedbackStatus status) {
        if (status.equals(RequestFeedbackStatus.SUBMITTED)) {
            throw new BusinessException(ErrorCode.REQUEST_FEEDBACK_ALREADY_SUBMITTED);
        }
    }

    public void validatePayloadType(RequestFeedbackType type, RequestFeedbackPayload payload) {
        if (!type.getPayloadClass().equals(payload.getClass())) {
            throw new BusinessException(ErrorCode.REQUEST_FEEDBACK_INVALID_PAYLOAD);
        }
    }

}