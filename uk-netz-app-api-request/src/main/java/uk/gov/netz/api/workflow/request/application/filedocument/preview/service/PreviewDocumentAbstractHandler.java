package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

@Service
@RequiredArgsConstructor
public abstract class PreviewDocumentAbstractHandler implements PreviewDocumentHandler {

    protected final RequestTaskService requestTaskService;

    @Transactional(readOnly = true)
    public FileDTO previewDocument(final Long taskId, final DecisionNotification decisionNotification) {

        this.validateTaskType(taskId);
        return this.generateDocument(taskId, decisionNotification);
    }

    private void validateTaskType(final Long taskId) {

        final RequestTaskType taskType = requestTaskService.findTaskById(taskId).getType();
        final boolean valid = this.getTaskTypes().contains(taskType.getCode());
        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_TEMPLATE_FOR_REQUEST_TASK);
        }
    }

    protected abstract List<String> getTaskTypes();
    
    protected abstract FileDTO generateDocument(final Long taskId, final DecisionNotification decisionNotification);
}
