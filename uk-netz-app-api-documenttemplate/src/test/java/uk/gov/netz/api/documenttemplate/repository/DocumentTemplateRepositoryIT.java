package uk.gov.netz.api.documenttemplate.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.AuditConfiguration;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, AuditConfiguration.class})
class DocumentTemplateRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private DocumentTemplateRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByTypeAndCompetentAuthority() {
        String documentTemplateType = DocumentTemplateType.IN_RFI;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .type(documentTemplateType)
                .competentAuthority(competentAuthority)
                .fileDocumentTemplateId(1L)
                .name("doc template name")
                .workflow("workflow")
                .build();

        entityManager.persist(documentTemplate);

        flushAndClear();

        Optional<DocumentTemplate> resultOpt =
            repo.findByTypeAndCompetentAuthority(documentTemplateType, competentAuthority);
        assertThat(resultOpt).isNotEmpty();
        assertThat(resultOpt.get().getName()).isEqualTo("doc template name");
    }

    @Test
    void findAllByNotificationTemplateId() {
    	Long notificationTemplateId = 1L;
    	Long anotherNotificationTemplateId = 2L;

        DocumentTemplate documentTemplate1 = DocumentTemplate.builder()
                .type(DocumentTemplateType.IN_RFI)
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .fileDocumentTemplateId(1L)
                .notificationTemplateId(notificationTemplateId)
                .name("doc template name")
                .workflow("workflow")
                .build();
        entityManager.persist(documentTemplate1);

        DocumentTemplate documentTemplate2 = DocumentTemplate.builder()
                .type(DocumentTemplateType.IN_RDE)
                .competentAuthority(CompetentAuthorityEnum.WALES)
                .fileDocumentTemplateId(2L)
                .notificationTemplateId(notificationTemplateId)
                .name("doc template name2")
                .workflow("workflow2")
                .build();
        entityManager.persist(documentTemplate2);

        DocumentTemplate documentTemplate3 = DocumentTemplate.builder()
                .type(DocumentTemplateType.IN_RFI)
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .fileDocumentTemplateId(3L)
                .notificationTemplateId(anotherNotificationTemplateId)
                .name("doc template name3")
                .workflow("workflow3")
                .build();
        entityManager.persist(documentTemplate3);

        flushAndClear();

        Set<DocumentTemplate> result =
            repo.findAllByNotificationTemplateId(notificationTemplateId);

		assertThat(result).extracting(DocumentTemplate::getName).containsExactlyInAnyOrder(documentTemplate1.getName(),
				documentTemplate2.getName());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
