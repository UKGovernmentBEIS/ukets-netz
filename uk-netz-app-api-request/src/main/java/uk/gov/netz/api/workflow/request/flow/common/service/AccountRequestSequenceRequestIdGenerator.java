package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestSequenceRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

public abstract class AccountRequestSequenceRequestIdGenerator extends RequestSequenceRequestIdGenerator {

    private static final String REQUEST_ID_FORMATTER = "%s%05d-%d";
    protected RequestTypeRepository requestTypeRepository;

    public AccountRequestSequenceRequestIdGenerator(RequestSequenceRepository repository, RequestTypeRepository requestTypeRepository) {
        super(repository);
        this.requestTypeRepository = requestTypeRepository;
    }

    protected RequestSequence resolveRequestSequence(RequestParams params) {
        final Long accountId = params.getAccountId();
        final String requestTypeCode = params.getType();
        
		final RequestType requestType = requestTypeRepository.findByCode(requestTypeCode)
				.orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_TYPE_NOT_FOUND));

		return repository.findByBusinessIdentifierAndRequestType(String.valueOf(accountId), requestType)
				.orElse(new RequestSequence(String.valueOf(accountId), requestType));
    }
    
    protected String generateRequestId(Long sequenceNo, RequestParams params) {
        return String.format(REQUEST_ID_FORMATTER, getPrefix(), 
        		params.getAccountId(), sequenceNo);
    }

}
