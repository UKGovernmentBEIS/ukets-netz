package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

@Service
public abstract class SyncPreviewDocumentAbstractHandler extends PreviewDocumentAbstractHandler implements SyncPreviewDocumentHandler {

    public SyncPreviewDocumentAbstractHandler(RequestTaskService requestTaskService) {
        super(requestTaskService);
    }

    @Transactional(readOnly = true)
    public FileDTO previewDocument(final Long taskId, final DecisionNotification decisionNotification) {

        this.validateTaskType(taskId);
        return this.generateDocument(taskId, decisionNotification);
    }

    protected abstract List<String> getTaskTypes();
    
    protected abstract FileDTO generateDocument(final Long taskId, final DecisionNotification decisionNotification);
}
