package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

@Service
public abstract class AsyncPreviewDocumentAbstractHandler extends PreviewDocumentAbstractHandler implements AsyncPreviewDocumentHandler {

    public AsyncPreviewDocumentAbstractHandler(RequestTaskService requestTaskService) {
        super(requestTaskService);
    }

    @Transactional
    public RequestTaskPayload previewDocumentAsync(final Long taskId, final DecisionNotification decisionNotification) {

        this.validateTaskType(taskId);
        return generateDocument(taskId, decisionNotification);
    }

    protected abstract List<String> getTaskTypes();
    
    protected abstract RequestTaskPayload generateDocument(final Long taskId, final DecisionNotification decisionNotification);
}
