package uk.gov.netz.api.notification.template.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class NotificationTemplateRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private NotificationTemplateRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByNameAndCompetentAuthority() {
        Optional<NotificationTemplate> result = repo.findByNameAndCompetentAuthority(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                CompetentAuthorityEnum.ENGLAND);
        assertThat(result).isEmpty();

        NotificationTemplate notificationTemplate = createNotificationTemplate(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                CompetentAuthorityEnum.ENGLAND,
                "subject",
                "content",
                "workflow",
                true);

        entityManager.persist(notificationTemplate);
        flushAndClear();

        result = repo.findByNameAndCompetentAuthority(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                CompetentAuthorityEnum.ENGLAND);
        assertThat(result).isNotEmpty();
    }

    @Test
    void findByNameAndCompetentAuthority_empty_ca() {
        Optional<NotificationTemplate> result = repo.findByNameAndCompetentAuthority(
                "INVITATION_TO_OPERATOR_ACCOUNT", null);
        assertThat(result).isEmpty();

        NotificationTemplate notificationTemplate = createNotificationTemplate(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                null,
                "subject",
                "content",
                "workflow",
                true);

        entityManager.persist(notificationTemplate);
        flushAndClear();

        result = repo.findByNameAndCompetentAuthority(
                "INVITATION_TO_OPERATOR_ACCOUNT", null);
        assertThat(result).isNotEmpty();
    }

    @Test
    void findManagedNotificationTemplateByIdWithText() {
        String permitWorkflow = "Permit Workflow";
        NotificationTemplate notificationTemplate1 = createNotificationTemplate(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                CompetentAuthorityEnum.WALES,
                "sub1",
                "text1",
                permitWorkflow,
                true);

        NotificationTemplate notificationTemplate2 = createNotificationTemplate(
                "INVITATION_TO_VERIFIER_ACCOUNT",
                CompetentAuthorityEnum.WALES,
                "sub2",
                "text2",
                permitWorkflow,
                true);


        entityManager.persist(notificationTemplate1);
        entityManager.persist(notificationTemplate2);

        flushAndClear();

        Optional<NotificationTemplate> resultOpt =
                repo.findManagedNotificationTemplateByIdWithText(notificationTemplate1.getId());

        assertThat(resultOpt).isNotEmpty();

        NotificationTemplate notificationTemplate = resultOpt.get();
        assertThat(notificationTemplate.getName()).isEqualTo("INVITATION_TO_OPERATOR_ACCOUNT");
        assertThat(notificationTemplate.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.WALES);
        assertThat(notificationTemplate.getSubject()).isEqualTo("sub1");
        assertThat(notificationTemplate.getText()).isEqualTo("text1");
    }

    @Test
    void findManagedNotificationTemplateByIdWithDocumentTemplates_not_managed() {
        String permitWorkflow = "Permit Workflow";
        NotificationTemplate notificationTemplate1 = createNotificationTemplate(
                "INVITATION_TO_OPERATOR_ACCOUNT",
                CompetentAuthorityEnum.WALES,
                "Invitation To Operator Account",
                "Invitation To Operator Account",
                permitWorkflow,
                false);

        entityManager.persist(notificationTemplate1);

        flushAndClear();

        Optional<NotificationTemplate> optionalNotificationTemplate =
                repo.findManagedNotificationTemplateByIdWithText(notificationTemplate1.getId());

        assertThat(optionalNotificationTemplate).isEmpty();
    }

    private NotificationTemplate createNotificationTemplate(String name, CompetentAuthorityEnum ca, String subject, String text,
                                                            String workflow, boolean managed) {
        return NotificationTemplate.builder()
                .name(name)
                .subject(subject)
                .text(text)
                .competentAuthority(ca)
                .workflow(workflow)
                .roleType(RoleTypeConstants.OPERATOR)
                .managed(managed)
                .lastUpdatedDate(LocalDateTime.now())
                .build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}