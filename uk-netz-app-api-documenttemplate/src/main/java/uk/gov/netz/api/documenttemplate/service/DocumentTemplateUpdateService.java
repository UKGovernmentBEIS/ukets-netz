package uk.gov.netz.api.documenttemplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.documents.service.FileDocumentTemplateService;

@Service
@RequiredArgsConstructor
public class DocumentTemplateUpdateService {

    private final DocumentTemplateQueryService documentTemplateQueryService;
    private final FileDocumentTemplateService fileDocumentTemplateService;

    @Transactional
    public void updateDocumentTemplateFile(Long documentTemplateId, FileDTO file, String authUserId) {
        final DocumentTemplate documentTemplate = documentTemplateQueryService.getDocumentTemplateById(documentTemplateId);
        fileDocumentTemplateService.deleteFileDocumentTemplateById(documentTemplate.getFileDocumentTemplateId());
        Long fileDocumentTemplateId = fileDocumentTemplateService.createFileDocumentTemplate(file, authUserId);
        documentTemplate.setFileDocumentTemplateId(fileDocumentTemplateId);        
    }
}
