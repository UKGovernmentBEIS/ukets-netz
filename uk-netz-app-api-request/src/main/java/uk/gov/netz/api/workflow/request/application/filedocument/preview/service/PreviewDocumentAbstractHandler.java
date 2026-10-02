package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import java.util.List;

@Service
@RequiredArgsConstructor
public abstract class PreviewDocumentAbstractHandler {

    protected final RequestTaskService requestTaskService;

    protected void validateTaskType(final Long taskId) {
        final RequestTaskType taskType = requestTaskService.findTaskById(taskId).getType();
        final boolean valid = this.getTaskTypes().contains(taskType.getCode());
        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_TEMPLATE_FOR_REQUEST_TASK);
        }
    }

    protected abstract List<String> getTaskTypes();
}
