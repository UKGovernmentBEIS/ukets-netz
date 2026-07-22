package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import uk.gov.netz.api.workflow.request.core.domain.Payload;

import java.util.Map;

public interface DocumentTemplateSyncWorkflowParamsProvider<T extends Payload>
		extends DocumentTemplateWorkflowParamsProvider {

    Map<String, Object> constructParams(T payload);
    
}
