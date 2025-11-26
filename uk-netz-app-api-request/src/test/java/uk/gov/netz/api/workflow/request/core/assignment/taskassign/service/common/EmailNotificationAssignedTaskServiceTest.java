package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailNotificationAssignedTaskServiceTest {

    @InjectMocks
    private EmailNotificationAssignedTaskService emailNotificationAssignedTaskService;

    @Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;

    @Mock
    private UserInfoApi userInfoApi;

    @Mock
    private WebAppProperties webAppProperties;

    private static final String USER_ID = "userId";
    private static final String EMAIL = "email@example.com";
    private static final String HOME_PAGE = "https://www.example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webAppProperties.getUrl()).thenReturn(HOME_PAGE);
    }

    @Test
    public void sendEmailToRecipient_shouldCallNotifyRecipient_whenUserIdNotNull() {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setEmail(EMAIL);
        when(userInfoApi.getUserByUserId(USER_ID)).thenReturn(userInfoDTO);

        emailNotificationAssignedTaskService.sendEmailToRecipient(USER_ID);

        verify(notificationEmailService, times(1)).notifyRecipient(any(EmailData.class),
            eq(EMAIL));
    }
}