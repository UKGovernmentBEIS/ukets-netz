package uk.gov.netz.api.workflow.request.flow.rfi.service;

import org.springframework.stereotype.Component;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateGenerationContextActionType;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateWorkflowParamsProvider;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RequestPayloadRfiable;

import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Component
public class RfiSubmitDocumentTemplateWorkflowParamsProvider implements DocumentTemplateWorkflowParamsProvider<RequestPayloadRfiable> {

    @Override
    public String getContextActionType() {
        return DocumentTemplateGenerationContextActionType.RFI_SUBMIT;
    }

    @Override
    public Map<String, Object> constructParams(RequestPayloadRfiable payload) {
        return Map.of(
                "deadline", Date.from(payload.getRfiData().getRfiDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                "questions", payload.getRfiData().getRfiQuestionPayload().getQuestions(),
                "isCorsia", false
                );
    }

}
