package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestSequenceRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestSequenceRequestIdGenerator;

import java.util.List;

@Service
public class SystemMessageNotificationRequestSequenceRequestIdGenerator extends RequestSequenceRequestIdGenerator {
    protected RequestTypeRepository requestTypeRepository;

	public SystemMessageNotificationRequestSequenceRequestIdGenerator(RequestSequenceRepository repository, RequestTypeRepository requestTypeRepository) {
		super(repository);
        this.requestTypeRepository = requestTypeRepository;
	}
	
	protected RequestSequence resolveRequestSequence(RequestParams params) {
        final RequestType requestType = requestTypeRepository.findByCode(params.getType())
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_TYPE_NOT_FOUND));

        return repository.findByRequestType(requestType).orElse(new RequestSequence(requestType));
	}
    
    protected String generateRequestId(Long sequenceNo, RequestParams params) {
    	return String.valueOf(sequenceNo);
    }

    @Override
    public List<String> getTypes() {
        return List.of(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION);
    }

    @Override
    public String getPrefix() {
        return null; //not applicable
    }
}
