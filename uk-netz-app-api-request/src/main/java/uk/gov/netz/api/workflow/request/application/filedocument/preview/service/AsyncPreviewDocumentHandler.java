package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

public interface AsyncPreviewDocumentHandler {

    RequestTaskPayload previewDocumentAsync(Long taskId, final DecisionNotification decisionNotification);

    List<String> getTypes();
}
