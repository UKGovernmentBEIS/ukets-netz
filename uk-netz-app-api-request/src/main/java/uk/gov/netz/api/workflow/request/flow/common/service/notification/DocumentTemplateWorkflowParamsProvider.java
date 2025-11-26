package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import uk.gov.netz.api.workflow.request.core.domain.Payload;

import java.util.Map;

public interface DocumentTemplateWorkflowParamsProvider<T extends Payload> {

    String getContextActionType();
    
    Map<String, Object> constructParams(T payload);
    
}
