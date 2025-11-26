package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.repository.RequestSequenceRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

@RequiredArgsConstructor
public abstract class RequestSequenceRequestIdGenerator implements RequestIdGenerator {

    protected final RequestSequenceRepository repository;

    @Transactional
    public String generate(RequestParams params) {
        final RequestSequence requestSequence = resolveRequestSequence(params);
        final Long sequenceNo = requestSequence.incrementSequenceAndGet();
        repository.save(requestSequence);

        return generateRequestId(sequenceNo, params);
    }
    
    protected abstract RequestSequence resolveRequestSequence(RequestParams params);
    
    protected abstract String generateRequestId(Long sequenceNo, RequestParams params);
}
