package uk.gov.netz.api.documenttemplate.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchCriteria;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchResults;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class DocumentTemplateCustomRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private DocumentTemplateCustomRepositoryImpl repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByCompetentAuthority_without_search_term() {
        String workflow = "workflow";
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        DocumentTemplate docTemplate1 = createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            competentAuthority, workflow, 1L);
        createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            CompetentAuthorityEnum.WALES, workflow, 2L);

        flushAndClear();

        DocumentTemplateSearchCriteria searchCriteria = DocumentTemplateSearchCriteria.builder()
            .competentAuthority(competentAuthority)
            .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
            .build();

        DocumentTemplateSearchResults searchResults = repo.findBySearchCriteria(searchCriteria);

        assertThat(searchResults.getTotal()).isEqualTo(1);

        List<DocumentTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(1);
        assertThat(notificationTemplates).extracting(DocumentTemplateInfoDTO::getName)
            .containsExactly(
                docTemplate1.getName()
            );
    }

    @Test
    void findByCompetentAuthority_with_search_term_in_workflow() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        DocumentTemplate docTemplate1 = createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            competentAuthority, "RFI sub-process",1L);
        createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            CompetentAuthorityEnum.WALES, "RFI sub-process", 2L);

        flushAndClear();

        DocumentTemplateSearchCriteria searchCriteria = DocumentTemplateSearchCriteria.builder()
            .competentAuthority(competentAuthority)
            .term("process")
            .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
            .build();

        DocumentTemplateSearchResults searchResults = repo.findBySearchCriteria(searchCriteria);

        assertThat(searchResults.getTotal()).isEqualTo(1);

        List<DocumentTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(1);
        assertThat(notificationTemplates).extracting(DocumentTemplateInfoDTO::getName)
            .containsExactly(
                docTemplate1.getName()
            );
    }

    @Test
    void findByCompetentAuthority_with_search_term_in_name() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        DocumentTemplate docTemplate1 = createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            competentAuthority, "RFI sub-process", 1L);
        createDocumentTemplate(DocumentTemplateType.IN_RFI, "IN Request for Further Information",
            CompetentAuthorityEnum.WALES, "RFI sub-process", 2L);

        flushAndClear();

        DocumentTemplateSearchCriteria searchCriteria = DocumentTemplateSearchCriteria.builder()
            .competentAuthority(competentAuthority)
            .term("Request")
            .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
            .build();

        DocumentTemplateSearchResults searchResults = repo.findBySearchCriteria(searchCriteria);

        assertThat(searchResults.getTotal()).isEqualTo(1);

        List<DocumentTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(1);
        assertThat(notificationTemplates).extracting(DocumentTemplateInfoDTO::getName)
            .containsExactly(
                docTemplate1.getName()
            );
    }

    private DocumentTemplate createDocumentTemplate(String type, String name, CompetentAuthorityEnum ca,
                                                    String workflow, Long fileDocumentTemplateId) {
        DocumentTemplate documentTemplate = DocumentTemplate.builder()
            .type(type)
            .name(name)
            .competentAuthority(ca)
            .workflow(workflow)
            .lastUpdatedDate(LocalDateTime.now())
            .fileDocumentTemplateId(fileDocumentTemplateId)
            .build();

        entityManager.persist(documentTemplate);

        return documentTemplate;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
