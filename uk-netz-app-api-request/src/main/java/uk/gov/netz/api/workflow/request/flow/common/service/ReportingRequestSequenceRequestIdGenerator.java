package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestMetadataReportable;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestSequenceRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

import java.time.Year;

public abstract class ReportingRequestSequenceRequestIdGenerator extends RequestSequenceRequestIdGenerator {

    protected static final String REQUEST_ID_FORMATTER = "%s%05d-%d-%d";
    protected RequestTypeRepository requestTypeRepository;

    protected ReportingRequestSequenceRequestIdGenerator(RequestSequenceRepository repository, RequestTypeRepository requestTypeRepository) {
        super(repository);
        this.requestTypeRepository = requestTypeRepository;
    }

    protected RequestSequence resolveRequestSequence(RequestParams params) {
        final Long accountId = params.getAccountId();
        final Year year = ((RequestMetadataReportable)params.getRequestMetadata()).getYear();
        final String requestTypeCode = params.getType();
        
        final RequestType requestType = requestTypeRepository.findByCode(requestTypeCode)
				.orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_TYPE_NOT_FOUND));
        
        final String businessIdentifierKey = accountId + "-" + year.getValue();
        
		return repository.findByBusinessIdentifierAndRequestType(businessIdentifierKey, requestType)
				.orElse(new RequestSequence(businessIdentifierKey, requestType));
    }

    @Override
    protected String generateRequestId(Long sequenceNo, RequestParams params) {

        return String.format(REQUEST_ID_FORMATTER,
            getPrefix(), params.getAccountId(),
            ((RequestMetadataReportable) params.getRequestMetadata()).getYear().getValue(),
            sequenceNo);
    }
}
