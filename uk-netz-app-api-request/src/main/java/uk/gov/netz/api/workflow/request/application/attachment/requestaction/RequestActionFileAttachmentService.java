package uk.gov.netz.api.workflow.request.application.attachment.requestaction;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.service.storage.FileAttachmentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;

@Service
@RequiredArgsConstructor
public class RequestActionFileAttachmentService {

	private final RequestActionRepository requestActionRepository;
    private final FileAttachmentStorageService fileAttachmentStorageService;
    
    @Transactional
    public FileToken generateGetFileAttachmentToken(Long requestActionId, UUID attachmentUuid) {
    	RequestAction requestAction = requestActionRepository.findById(requestActionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

		if (!requestAction.getPayload().getAttachments().containsKey(attachmentUuid)) {
			throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, attachmentUuid);
		}
        
        return fileAttachmentStorageService.generateGetFileAttachmentToken(attachmentUuid.toString());
    }
    
}
