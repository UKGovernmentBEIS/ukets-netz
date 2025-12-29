package uk.gov.netz.api.notification.template.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateDTO;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateInfoDTO;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {NotificationTemplateMapperImpl.class})
class NotificationTemplateMapperTest {

    @Autowired
    private NotificationTemplateMapper notificationTemplateMapper;

    @Test
    void toNotificationTemplateDTO() {
        Long notificationTemplateId = 1L;
        String notificationTemplateName = "EMAIL_CONFIRMATION";
        String notificationTemplateSubject = "notification template subject";
        String notificationTemplateText = "notification template text";
        String workflow = " workflow";
        String eventTrigger = "event trigger the notification";

        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
            .id(notificationTemplateId)
            .name(notificationTemplateName)
            .subject(notificationTemplateSubject)
            .text(notificationTemplateText)
            .competentAuthority(CompetentAuthorityEnum.WALES)
            .workflow(workflow)
            .roleType(RoleTypeConstants.OPERATOR)
            .eventTrigger(eventTrigger)
            .managed(true)
            .lastUpdatedDate(LocalDateTime.now())
            .build();

        NotificationTemplateDTO notificationTemplateDTO = notificationTemplateMapper.toNotificationTemplateDTO(notificationTemplate);

        assertNotNull(notificationTemplateDTO);
        assertEquals(notificationTemplateId, notificationTemplateDTO.getId());
        assertEquals(notificationTemplateName, notificationTemplateDTO.getName());
        assertEquals(notificationTemplateSubject, notificationTemplateDTO.getSubject());
        assertEquals(notificationTemplateText, notificationTemplateDTO.getText());
        assertEquals(workflow, notificationTemplateDTO.getWorkflow());
        assertEquals(eventTrigger, notificationTemplateDTO.getEventTrigger());
    }
    
    @Test
    void toNotificationTemplateInfoDTO() {
        Long notificationTemplateId = 1L;
        String notificationTemplateName = "EMAIL_CONFIRMATION";
        String workflow = " workflow";
        String roleType = RoleTypeConstants.OPERATOR;
        LocalDateTime lastUpdatedDate = LocalDateTime.now();

        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
            .id(notificationTemplateId)
            .name(notificationTemplateName)
            .roleType(roleType)
            .workflow(workflow)
            .lastUpdatedDate(lastUpdatedDate)
            .build();

        NotificationTemplateInfoDTO result = notificationTemplateMapper.toNotificationTemplateInfoDTO(notificationTemplate);

		assertThat(result).isEqualTo(new NotificationTemplateInfoDTO(notificationTemplateId,
				notificationTemplateName, RoleTypeConstants.OPERATOR, workflow, lastUpdatedDate));
    }

}
