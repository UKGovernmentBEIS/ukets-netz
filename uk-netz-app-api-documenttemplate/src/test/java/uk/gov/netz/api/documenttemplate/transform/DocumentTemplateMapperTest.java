package uk.gov.netz.api.documenttemplate.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateDTO;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {DocumentTemplateMapperImpl.class})
class DocumentTemplateMapperTest {

    @Autowired
    private DocumentTemplateMapper documentTemplateMapper;

    @Test
    void toDocumentTemplateDTO() {
        Long documentTemplateId = 1L;
        String documentTemplateName = "document template name";
        String workflow = " workflow";
        String fileUuid = UUID.randomUUID().toString();
        String filename = "filename";

        DocumentTemplate documentTemplate = DocumentTemplate.builder()
            .id(documentTemplateId)
            .type(DocumentTemplateType.IN_RFI)
            .name(documentTemplateName)
            .competentAuthority(CompetentAuthorityEnum.WALES)
            .notificationTemplateId(1L)
            .workflow(workflow)
            .lastUpdatedDate(LocalDateTime.now())
            .build();

        FileInfoDTO fileDocumentDTO = FileInfoDTO.builder().uuid(fileUuid).name(filename).build();

        DocumentTemplateDTO documentTemplateDTO = documentTemplateMapper.toDocumentTemplateDTO(documentTemplate, fileDocumentDTO);

        assertNotNull(documentTemplateDTO);
        assertEquals(documentTemplateId, documentTemplateDTO.getId());
        assertEquals(documentTemplateName, documentTemplateDTO.getName());
        assertEquals(workflow, documentTemplateDTO.getWorkflow());
        assertEquals(fileUuid, documentTemplateDTO.getFileUuid());
        assertEquals(filename, documentTemplateDTO.getFilename());
        assertThat(documentTemplateDTO.getNotificationTemplateId()).isEqualTo(1L);
    }
    
    @Test
    void toDocumentTemplateInfoDTO() {
    	LocalDateTime now = LocalDateTime.now();
    	DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .id(1L)
                .type(DocumentTemplateType.IN_RFI)
                .name("name")
                .roleType("OPERATOR")
                .competentAuthority(CompetentAuthorityEnum.WALES)
                .notificationTemplateId(1L)
                .workflow("workflow")
                .lastUpdatedDate(now)
                .build();


    	DocumentTemplateInfoDTO result = documentTemplateMapper.toDocumentTemplateInfoDTO(documentTemplate);
    	
		assertThat(result).isEqualTo(new DocumentTemplateInfoDTO(1L, "name", "OPERATOR", "workflow", now));
    }

}
