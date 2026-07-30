package uk.gov.netz.api.documenttemplate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.documents.service.FileDocumentTemplateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateUpdateServiceTest {

    @InjectMocks
    private DocumentTemplateUpdateService service;

    @Mock
    private DocumentTemplateQueryService documentTemplateQueryService;

    @Mock
    private FileDocumentTemplateService fileDocumentTemplateService;

    @Test
    void updateDocumentTemplateFile() {
        Long documentTemplateId = 1L;
        FileDTO file = FileDTO.builder()
                .fileName("name")
                .createdBy("user")
                .build();
        
        Long existingFileDocumentTemplateId = 2L;
        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .id(documentTemplateId)
                .type(DocumentTemplateType.IN_RFI)
                .name("name")
                .workflow("workflow")
                .fileDocumentTemplateId(existingFileDocumentTemplateId)
                .build();
        
        Long fileDocumentTemplateId = 5L;
        
        when(documentTemplateQueryService.getDocumentTemplateById(documentTemplateId)).thenReturn(documentTemplate);
        when(fileDocumentTemplateService.createFileDocumentTemplate(file)).thenReturn(fileDocumentTemplateId);

        service.updateDocumentTemplateFile(documentTemplateId, file);

        assertThat(documentTemplate.getFileDocumentTemplateId()).isEqualTo(fileDocumentTemplateId);
        verify(documentTemplateQueryService, times(1)).getDocumentTemplateById(documentTemplateId);
        verify(fileDocumentTemplateService, times(1)).deleteFileDocumentTemplateById(existingFileDocumentTemplateId);
        verify(fileDocumentTemplateService, times(1)).createFileDocumentTemplate(file);
    }

}
