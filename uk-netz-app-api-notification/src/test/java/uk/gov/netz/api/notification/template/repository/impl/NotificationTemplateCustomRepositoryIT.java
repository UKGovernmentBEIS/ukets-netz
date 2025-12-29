package uk.gov.netz.api.notification.template.repository.impl;

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
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateInfoDTO;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchCriteria;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchResults;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateCustomRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class NotificationTemplateCustomRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private NotificationTemplateCustomRepositoryImpl repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByCompetentAuthority_with_search_term() {
        String permitWorkflow = "Permit Workflow";
        String accountOpeningWorkflow = "Account Opening Workflow";
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        NotificationTemplate notificationTemplate1 = createNotificationTemplate("INVITATION_TO_REGULATOR_ACCOUNT",
            competentAuthority, permitWorkflow, RoleTypeConstants.OPERATOR, true);
        NotificationTemplate notificationTemplate2 = createNotificationTemplate("INVITATION_TO_OPERATOR_ACCOUNT",
            competentAuthority, permitWorkflow, RoleTypeConstants.OPERATOR, true);
        NotificationTemplate notificationTemplate3 = createNotificationTemplate("USER_ACCOUNT_CREATED",
            competentAuthority, accountOpeningWorkflow, RoleTypeConstants.OPERATOR, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION", competentAuthority, accountOpeningWorkflow,
            RoleTypeConstants.REGULATOR, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION", CompetentAuthorityEnum.WALES, accountOpeningWorkflow,
            RoleTypeConstants.OPERATOR, true);
        createNotificationTemplate("CHANGE_2FA", null, null, null, false);

        flushAndClear();

        NotificationTemplateSearchCriteria searchCriteria = NotificationTemplateSearchCriteria.builder()
            .competentAuthority(competentAuthority)
            .roleTypes(List.of(RoleTypeConstants.OPERATOR))
            .term("account")
            .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
            .build();

        NotificationTemplateSearchResults searchResults = repo.findBySearchCriteria(searchCriteria);

        assertThat(searchResults.getTotal()).isEqualTo(3);

        List<NotificationTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(3);
        assertThat(notificationTemplates).extracting(NotificationTemplateInfoDTO::getName)
            .containsExactly(
                notificationTemplate2.getName(),
                notificationTemplate1.getName(),
                notificationTemplate3.getName()
            );
    }

    @Test
    void findByCompetentAuthority_without_search_term() {
        String permitWorkflow = "Permit Workflow";
        String accountOpeningWorkflow = "Account Opening Workflow";
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        createNotificationTemplate("INVITATION_TO_EMITTER_CONTACT", competentAuthority, permitWorkflow,
            RoleTypeConstants.OPERATOR, true);
        NotificationTemplate notificationTemplate2 = createNotificationTemplate("USER_ACCOUNT_CREATED",
            competentAuthority, accountOpeningWorkflow, RoleTypeConstants.REGULATOR, true);
        NotificationTemplate notificationTemplate3 = createNotificationTemplate("USER_ACCOUNT_ACTIVATION",
            competentAuthority, accountOpeningWorkflow, RoleTypeConstants.REGULATOR, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION", CompetentAuthorityEnum.WALES, accountOpeningWorkflow,
            RoleTypeConstants.OPERATOR, true);
        createNotificationTemplate("CHANGE_2FA", null, null, null ,false);

        flushAndClear();

        NotificationTemplateSearchCriteria searchCriteria = NotificationTemplateSearchCriteria.builder()
            .competentAuthority(competentAuthority)
            .roleTypes(List.of(RoleTypeConstants.REGULATOR))
            .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
            .build();

        NotificationTemplateSearchResults
            searchResults = repo.findBySearchCriteria(searchCriteria);

        assertThat(searchResults.getTotal()).isEqualTo(2);

        List<NotificationTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(2);
        assertThat(notificationTemplates).extracting(NotificationTemplateInfoDTO::getName)
            .containsExactly(
                notificationTemplate3.getName(),
                notificationTemplate2.getName()
            );
    }
    
    @Test
    void findByCompetentAuthority_without_role_type_defined() {
        String permitWorkflow = "Permit Workflow";
        String accountOpeningWorkflow = "Account Opening Workflow";
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        
        createNotificationTemplate("INVITATION_TO_EMITTER_CONTACT",
                competentAuthority, permitWorkflow, RoleTypeConstants.OPERATOR, true);
        createNotificationTemplate("USER_ACCOUNT_CREATED",
                competentAuthority, accountOpeningWorkflow, RoleTypeConstants.REGULATOR, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION",
                competentAuthority, accountOpeningWorkflow, RoleTypeConstants.REGULATOR, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION",
                competentAuthority, accountOpeningWorkflow, null, true);
        createNotificationTemplate("USER_ACCOUNT_ACTIVATION",
                CompetentAuthorityEnum.WALES, accountOpeningWorkflow, RoleTypeConstants.OPERATOR, true);
        createNotificationTemplate("CHANGE_2FA", null, null, null ,false);
        
        flushAndClear();
        
        NotificationTemplateSearchCriteria searchCriteria = NotificationTemplateSearchCriteria.builder()
                .competentAuthority(competentAuthority)
                .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
                .build();
        
        NotificationTemplateSearchResults
                searchResults = repo.findBySearchCriteria(searchCriteria);
        
        assertThat(searchResults.getTotal()).isEqualTo(4);
        
        List<NotificationTemplateInfoDTO> notificationTemplates = searchResults.getTemplates();
        assertThat(notificationTemplates).hasSize(4);
    }


    private NotificationTemplate createNotificationTemplate(String name, CompetentAuthorityEnum ca, String workflow,
    		String roleType, boolean managed) {
        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
            .name(name)
            .subject("subject")
            .text("text")
            .competentAuthority(ca)
            .workflow(workflow)
            .roleType(roleType)
            .managed(managed)
            .lastUpdatedDate(LocalDateTime.now())
            .build();

        entityManager.persist(notificationTemplate);

        return notificationTemplate;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
