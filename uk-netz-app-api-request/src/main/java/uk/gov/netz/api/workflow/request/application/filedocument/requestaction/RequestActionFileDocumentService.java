package uk.gov.netz.api.workflow.request.application.filedocument.requestaction;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.documents.service.storage.FileDocumentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;

@Service
@RequiredArgsConstructor
public class RequestActionFileDocumentService {

    private final RequestActionRepository requestActionRepository;
    private final FileDocumentStorageService fileDocumentStorageService;
    
    @Transactional
    public FileToken generateGetFileDocumentToken(Long requestActionId, UUID fileDocumentUuid) {
        final RequestAction requestAction = requestActionRepository.findById(requestActionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!requestAction.getPayload().getFileDocuments().containsKey(fileDocumentUuid)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, fileDocumentUuid);
        }
        
        return fileDocumentStorageService.generateGetFileDocumentToken(fileDocumentUuid.toString());
    }
    
}
