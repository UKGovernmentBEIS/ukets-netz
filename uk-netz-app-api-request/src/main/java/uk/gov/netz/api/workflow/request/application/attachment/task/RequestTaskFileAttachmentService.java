package uk.gov.netz.api.workflow.request.application.attachment.task;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.service.storage.FileAttachmentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

@Service
@RequiredArgsConstructor
public class RequestTaskFileAttachmentService {

	private final RequestTaskService requestTaskService;
    private final FileAttachmentStorageService fileAttachmentStorageService;
    
    @Transactional
    public FileToken generateGetFileAttachmentToken(Long requestTaskId, UUID attachmentUuid) {
    	RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        if (!requestTask.getPayload().getAttachments().containsKey(attachmentUuid)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, attachmentUuid);
        }
        
        return fileAttachmentStorageService.generateGetFileAttachmentToken(attachmentUuid.toString());
    }
    
}
