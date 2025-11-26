package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskAttachmentsUncoupleService;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RequestPayloadRfiable;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmitRequestTaskPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfiResponseSubmitInitializer implements InitializeRequestTaskHandler {

	private final RequestTaskTypeRepository requestTaskTypeRepository;
    private final RequestTaskAttachmentsUncoupleService uncoupleService;

    @Override
    public RequestTaskPayload initializePayload(final Request request) {

        final RequestPayloadRfiable requestPayload = (RequestPayloadRfiable) request.getPayload();
        final Map<UUID, String> requestRfiAttachments = requestPayload.getRfiData().getRfiAttachments();

        final RfiResponseSubmitRequestTaskPayload requestTaskPayload =
            RfiResponseSubmitRequestTaskPayload.builder()
                .payloadType(RequestTaskPayloadTypes.RFI_RESPONSE_SUBMIT_PAYLOAD)
                .rfiQuestionPayload(requestPayload.getRfiData().getRfiQuestionPayload())
                .rfiAttachments(new HashMap<>(requestRfiAttachments))
                .build();
        
        requestRfiAttachments.clear();

        uncoupleService.uncoupleAttachments(requestTaskPayload);

        return requestTaskPayload;
    }

    @Override
    public Set<String> getRequestTaskTypes() {
    	return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.RFI_RESPONSE_SUBMIT).stream()
				.map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
