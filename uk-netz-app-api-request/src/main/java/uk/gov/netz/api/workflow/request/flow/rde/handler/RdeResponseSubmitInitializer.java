package uk.gov.netz.api.workflow.request.flow.rde.handler;

import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeResponsePayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeResponseRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;
import uk.gov.netz.api.workflow.request.flow.rde.mapper.RdeMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RdeResponseSubmitInitializer implements InitializeRequestTaskHandler {
	
	private final RequestTaskTypeRepository requestTaskTypeRepository;
    private static final RdeMapper rdeMapper = Mappers.getMapper(RdeMapper.class);

    @Override
    public RequestTaskPayload initializePayload(final Request request) {
        final RequestPayloadRdeable requestPayload = (RequestPayloadRdeable) request.getPayload();
        final RdeResponsePayload rdeResponsePayload = rdeMapper.toRdeResponsePayload(
                requestPayload.getRdeData().getCurrentDueDate(),
                requestPayload.getRdeData().getRdePayload().getExtensionDate());

        return RdeResponseRequestTaskPayload.builder()
                .payloadType(RequestTaskPayloadTypes.RDE_RESPONSE_SUBMIT_PAYLOAD)
                .rdeResponsePayload(rdeResponsePayload)
                .build();
    }

    @Override
    public Set<String> getRequestTaskTypes() {
    	return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.RDE_RESPONSE_SUBMIT).stream()
				.map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
