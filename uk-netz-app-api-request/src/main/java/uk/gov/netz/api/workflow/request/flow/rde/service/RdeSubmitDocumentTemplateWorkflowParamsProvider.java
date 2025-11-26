package uk.gov.netz.api.workflow.request.flow.rde.service;

import org.springframework.stereotype.Component;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateGenerationContextActionType;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateWorkflowParamsProvider;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;

import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Component
public class RdeSubmitDocumentTemplateWorkflowParamsProvider implements DocumentTemplateWorkflowParamsProvider<RequestPayloadRdeable> {

    @Override
    public String getContextActionType() {
        return DocumentTemplateGenerationContextActionType.RDE_SUBMIT;
    }
    
    @Override
    public Map<String, Object> constructParams(RequestPayloadRdeable payload) {
        return Map.of(
                "extensionDate", Date.from(payload.getRdeData().getRdePayload().getExtensionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                "deadline", Date.from(payload.getRdeData().getRdePayload().getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                "isCorsia", false
                );
    }

}

