package uk.gov.netz.api.request.feedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackDetailsDTO;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackPayload;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackStatus;
import uk.gov.netz.api.request.feedback.domain.RequestFeedbackType;
import uk.gov.netz.api.request.feedback.repository.RequestFeedbackRepository;
import uk.gov.netz.api.request.feedback.transform.RequestFeedbackMapper;
import uk.gov.netz.api.request.feedback.utils.RequestFeedbackUtils;
import uk.gov.netz.api.request.feedback.validation.RequestFeedbackValidator;

@Service
@RequiredArgsConstructor
@Log4j2
public class RequestFeedbackService {

    private final RequestFeedbackQueryService queryService;
    private final RequestFeedbackRepository repository;
    private final RequestFeedbackMapper mapper;
    private final DateService dateService;
    private final RequestFeedbackValidator validator;

    @Value("${feature-flag.request.feedback.enabled}")
    private boolean requestFeedbackEnabled;

    @Transactional
    public void createRequestFeedback(String requestId, String type, AppUser appUser) {

        if (!requestFeedbackEnabled) {
            log.debug("Request feedback disabled. RequestId={}, type={}", requestId, type);
            return;
        }

        if (repository.existsByRequestIdAndType(requestId, type)) {
            log.info("Request feedback already exists for requestId={} and type={}", requestId, type);
            return;
        }

        RequestFeedback requestFeedback = RequestFeedback.builder()
            .requestId(requestId)
            .status(RequestFeedbackStatus.NEW)
            .type(type)
            .createdOn(dateService.getLocalDateTime())
            .createdBy(appUser.getFullName())
            .createdById(appUser.getUserId())
            .build();

        repository.save(requestFeedback);
    }

    @Transactional(readOnly = true)
    public RequestFeedbackDetailsDTO getRequestFeedbackDetails(String requestId, RequestFeedbackType type) {

        RequestFeedback requestFeedback = queryService.getRequestFeedback(requestId, type.getName());

        boolean expired = RequestFeedbackUtils.hasExpired(dateService.getLocalDateTime(), requestFeedback.getCreatedOn());

        return mapper.mapToDTO(requestFeedback, expired);
    }

    @Transactional
    public void submitRequestFeedback(String requestId, RequestFeedbackType type,
                                      RequestFeedbackPayload payload, AppUser appUser) {

        validator.validatePayloadType(type, payload);

        RequestFeedback requestFeedback = queryService.getRequestFeedback(requestId, type.getName());

        validator.validateStatus(requestFeedback.getStatus());
        validator.validateNotExpired(requestFeedback.getCreatedOn());

        requestFeedback.setSubmitter(appUser.getFullName());
        requestFeedback.setSubmitterId(appUser.getUserId());
        requestFeedback.setPayload(payload);
        requestFeedback.setSubmissionDate(dateService.getLocalDateTime());
        requestFeedback.setStatus(RequestFeedbackStatus.SUBMITTED);
    }


}
