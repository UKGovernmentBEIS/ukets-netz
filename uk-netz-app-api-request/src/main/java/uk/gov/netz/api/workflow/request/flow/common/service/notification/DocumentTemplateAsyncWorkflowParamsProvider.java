package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import java.util.Map;

import uk.gov.netz.api.workflow.request.core.domain.RequestTask;

public interface DocumentTemplateAsyncWorkflowParamsProvider extends DocumentTemplateWorkflowParamsProvider {

	Map<String, Object> constructParams(RequestTask requestTask);

}
