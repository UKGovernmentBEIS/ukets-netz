package uk.gov.netz.api.user.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.user.NotificationTemplateConstants;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.token.JwtTokenAction;
import uk.gov.netz.api.token.JwtTokenService;
import uk.gov.netz.api.user.NavigationParams;
import uk.gov.netz.api.user.NotificationTemplateName;
import uk.gov.netz.api.user.core.domain.model.UserNotificationWithRedirectionLinkInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.user.NavigationOutcomes.OPERATOR_REGISTRATION_INVITATION_ACCEPTED_URL;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @InjectMocks
    private UserNotificationService userNotificationService;

    @Mock
    private UserInfoApi userInfoApi;

    @Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private WebAppProperties webAppProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NotificationProperties notificationProperties;

    @Test
    void notifyInvitedUser() {
        String token = "token";
        String url = "http://www.home.org.uk";

        UserNotificationWithRedirectionLinkInfo notificationInfo = createNotificationInfo();
        UserNotificationWithRedirectionLinkInfo.TokenParams tokenParams = notificationInfo.getTokenParams();
        when(webAppProperties.getUrl()).thenReturn(url);

        when(jwtTokenService
            .generateToken(tokenParams.getJwtTokenAction(), tokenParams.getClaimValue(), tokenParams.getExpirationInterval()))
            .thenReturn(token);

        userNotificationService.notifyUserWithLink(notificationInfo);

        verify(jwtTokenService, times(1))
            .generateToken(tokenParams.getJwtTokenAction(), tokenParams.getClaimValue(), tokenParams.getExpirationInterval());

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor =
            ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(notificationInfo.getUserEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();

        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(notificationInfo.getTemplateName());
        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                NotificationTemplateConstants.APPLICANT_FNAME, "firstName",
                NotificationTemplateConstants.OPERATOR_INVITATION_CONFIRMATION_LINK, expectedNotificationLink(token, webAppProperties)
            ));
        assertThat(emailInfo.getAttachments()).isEmpty();
    }

    @Test
    void notifyUserAccountActivation() {
        String userId = "userId";
        String roleName = "roleName";
        String email = "email";
        UserInfoDTO userInfo = UserInfoDTO.builder().email(email).build();
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.USER_ACCOUNT_ACTIVATION)
                        .templateParams(Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, roleName,
                                NotificationTemplateConstants.CONTACT_REGULATOR, "helpdesk@netz.com",
                                NotificationTemplateConstants.HOME_URL, "url"))
                        .build())
                .build();

        when(userInfoApi.getUserByUserId(userId)).thenReturn(userInfo);
        when(notificationProperties.getEmail().getContactUsLink()).thenReturn("helpdesk@netz.com");
        when(webAppProperties.getUrl()).thenReturn("url");

        userNotificationService.notifyUserAccountActivation(userId, roleName);

        verify(userInfoApi, times(1)).getUserByUserId(userId);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfo, email);
    }

    @Test
    void notifyNewEmitterContact() {
        String userId = "userId";
        String accountName = "accountName";
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email").build();
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.INVITATION_TO_EMITTER_CONTACT)
                        .templateParams(Map.of(NotificationTemplateConstants.APPLICANT_FNAME, userInfo.getFirstName(),
                        		NotificationTemplateConstants.APPLICANT_LNAME, userInfo.getLastName(),
                                NotificationTemplateConstants.ACCOUNT_NAME, accountName,
                                NotificationTemplateConstants.CONTACT_REGULATOR, "link"))
                        .build())
                .build();

        when(userInfoApi.getUserByUserId(userId)).thenReturn(userInfo);
        when(notificationProperties.getEmail().getContactUsLink()).thenReturn("link");

        userNotificationService.notifyEmitterContactAccountActivation(userId, accountName);

        verify(userInfoApi, times(1)).getUserByUserId(userId);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfo, userInfo.getEmail());
    }
    
    @Test
    void notifyUserPasswordReset() {
        String userId = "userId";
        String email = "email";
        String url = "http://www.home.org.uk";
        String contactUsLink = url + "/contact-us";
        UserInfoDTO userInfo = UserInfoDTO.builder().email(email).build();
        when(webAppProperties.getUrl()).thenReturn(url);
        NotificationProperties.Email notificationEmail = mock(NotificationProperties.Email.class);
        when(notificationProperties.getEmail()).thenReturn(notificationEmail);
        when(notificationEmail.getContactUsLink()).thenReturn(contactUsLink);
        
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.RESET_PASSWORD_CONFIRMATION)
                        .templateParams(Map.of(
                        		NotificationTemplateConstants.HOME_URL, url,
                        		NotificationTemplateConstants.CONTACT_REGULATOR, contactUsLink))
                        .build())
                .build();

        when(userInfoApi.getUserByUserId(userId)).thenReturn(userInfo);

        userNotificationService.notifyUserPasswordReset(userId);

        verify(userInfoApi, times(1)).getUserByUserId(userId);
        verify(webAppProperties, times(1)).getUrl();
        verify(notificationProperties, times(1)).getEmail();
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfo, email);
    }
    
    @Test
    void notifyUserReset2Fa() {
        String userId = "userId";
        String email = "email";
        String url = "http://www.home.org.uk";
        String helpdesk = "info@netz.com";
        UserInfoDTO userInfo = UserInfoDTO.builder().email(email).build();
        when(webAppProperties.getUrl()).thenReturn(url);
        NotificationProperties.Email notificationEmail = mock(NotificationProperties.Email.class);
        when(notificationProperties.getEmail()).thenReturn(notificationEmail);
        when(notificationEmail.getContactUsLink()).thenReturn(helpdesk);
        
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.RESET_2FA_CONFIRMATION)
                        .templateParams(Map.of(
                        		NotificationTemplateConstants.HOME_URL, url,
                        		NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk))
                        .build())
                .build();

        when(userInfoApi.getUserByUserId(userId)).thenReturn(userInfo);

        userNotificationService.notifyUserReset2Fa(userId);

        verify(userInfoApi, times(1)).getUserByUserId(userId);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfo, email);
    }

    private UserNotificationWithRedirectionLinkInfo createNotificationInfo() {
        return UserNotificationWithRedirectionLinkInfo.builder()
            .templateName(NotificationTemplateName.INVITATION_TO_OPERATOR_ACCOUNT)
            .userEmail("email")
            .linkParamName(NotificationTemplateConstants.OPERATOR_INVITATION_CONFIRMATION_LINK)
            .linkPath(OPERATOR_REGISTRATION_INVITATION_ACCEPTED_URL)
            .notificationParams(new HashMap<>(
                Map.of(NotificationTemplateConstants.APPLICANT_FNAME, "firstName")))
            .tokenParams(UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                .jwtTokenAction(JwtTokenAction.OPERATOR_INVITATION)
                .claimValue("claimValue")
                .expirationInterval(5L)
                .build()
            )
            .build();
    }

    private String expectedNotificationLink(String token, WebAppProperties webAppProperties) {
        return UriComponentsBuilder
            .fromUriString(webAppProperties.getUrl())
            .path("/")
            .path(OPERATOR_REGISTRATION_INVITATION_ACCEPTED_URL)
            .queryParam(NavigationParams.TOKEN, token)
            .build()
            .toUriString();
    }
}