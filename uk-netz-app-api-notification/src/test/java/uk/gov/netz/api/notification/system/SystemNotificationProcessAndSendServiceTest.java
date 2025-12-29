package uk.gov.netz.api.notification.system;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SendSystemNotificationService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;

@ExtendWith(MockitoExtension.class)
class SystemNotificationProcessAndSendServiceTest {

	@InjectMocks
    private SystemNotificationProcessAndSendService cut;

    @Mock
    private NotificationTemplateProcessService notificationTemplateProcessService;

    @Mock
    private SendSystemNotificationService sendSystemNotificationService;

    @Test
    void processAndSend() {
        final String messageSubject = "message_subject";
        final String messageText = "message_text";
        final String receiver = "receiver";
        final Long accountId = 1L;
        SystemNotificationInfo msgInfo = SystemNotificationInfo.builder()
            .template("tmpl")
            .accountId(accountId)
            .receiver(receiver)
            .parameters(Map.of("param1", "val1"))
            .build();

        NotificationContent notificationContent = NotificationContent.builder()
            .text(messageText)
            .subject(messageSubject)
            .build();

        //mock
        when(notificationTemplateProcessService.processNotificationTemplate(msgInfo.getTemplate(),
            msgInfo.getParameters())).thenReturn(notificationContent);

        //invoke
        cut.processAndSend(msgInfo);

        //verify
        verify(notificationTemplateProcessService, times(1))
            .processNotificationTemplate(msgInfo.getTemplate(),
                    msgInfo.getParameters());
        verify(sendSystemNotificationService, times(1))
            .send(msgInfo, notificationContent);
    }
    
}
