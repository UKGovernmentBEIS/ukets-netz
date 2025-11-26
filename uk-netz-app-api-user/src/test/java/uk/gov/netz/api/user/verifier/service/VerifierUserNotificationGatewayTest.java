package uk.gov.netz.api.user.verifier.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.user.NotificationTemplateConstants;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.token.JwtProperties;
import uk.gov.netz.api.token.JwtTokenAction;
import uk.gov.netz.api.user.NavigationOutcomes;
import uk.gov.netz.api.user.NotificationTemplateName;
import uk.gov.netz.api.user.core.domain.model.UserNotificationWithRedirectionLinkInfo;
import uk.gov.netz.api.user.core.service.UserNotificationService;
import uk.gov.netz.api.user.verifier.domain.VerifierUserInvitationDTO;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifierUserNotificationGatewayTest {

    @InjectMocks
    private VerifierUserNotificationGateway verifierUserNotificationGateway;

    @Mock
    private UserNotificationService userNotificationService;

    @Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebAppProperties webAppProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NotificationProperties notificationProperties;

    @Test
    void notifyInvitedUser() {
        VerifierUserInvitationDTO verifierUserInvitation = createVerifierUserInvitationDTO();
        String authorityUuid = "uuid";
        JwtProperties.Claim claim = mock(JwtProperties.Claim.class);
        long expirationInterval = 60L;

        when(jwtProperties.getClaim()).thenReturn(claim);
        when(notificationProperties.getEmail().getContactUsLink()).thenReturn("link");

        when(claim.getUserInvitationExpIntervalMinutes()).thenReturn(expirationInterval);

        // Invoke
        verifierUserNotificationGateway.notifyInvitedUser(verifierUserInvitation, authorityUuid);

        // Verify
        ArgumentCaptor<UserNotificationWithRedirectionLinkInfo> notificationInfoCaptor =
                ArgumentCaptor.forClass(UserNotificationWithRedirectionLinkInfo.class);
        verify(userNotificationService, times(1)).notifyUserWithLink(notificationInfoCaptor.capture());

        UserNotificationWithRedirectionLinkInfo notificationInfo = notificationInfoCaptor.getValue();

        assertThat(notificationInfo.getTemplateName()).isEqualTo(NotificationTemplateName.INVITATION_TO_VERIFIER_ACCOUNT);
        assertThat(notificationInfo.getUserEmail()).isEqualTo(verifierUserInvitation.getEmail());
        assertThat(notificationInfo.getLinkParamName()).isEqualTo(NotificationTemplateConstants.VERIFIER_INVITATION_CONFIRMATION_LINK);
        assertThat(notificationInfo.getLinkPath()).isEqualTo(NavigationOutcomes.VERIFIER_REGISTRATION_INVITATION_ACCEPTED_URL);
        assertThat(notificationInfo.getNotificationParams()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        NotificationTemplateConstants.APPLICANT_FNAME, verifierUserInvitation.getFirstName(),
                        NotificationTemplateConstants.APPLICANT_LNAME, verifierUserInvitation.getLastName(),
                        NotificationTemplateConstants.EXPIRATION_MINUTES, 60L,
                        NotificationTemplateConstants.CONTACT_REGULATOR, "link"
                ));
        assertThat(notificationInfo.getTokenParams()).isEqualTo(expectedInvitationLinkTokenParams(authorityUuid, expirationInterval));
    }

    @Test
    void notifyInviteeAcceptedInvitation() {
        UserInfoDTO invitee = UserInfoDTO.builder()
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .build();

        when(notificationProperties.getEmail().getContactUsLink()).thenReturn("link");
        when(webAppProperties.getUrl()).thenReturn("url");

        // Invoke
        verifierUserNotificationGateway.notifyInviteeAcceptedInvitation(invitee);

        // Verify
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(invitee.getEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();
        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(NotificationTemplateName.INVITEE_INVITATION_ACCEPTED);
        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(
                Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, "Verifier",
                        NotificationTemplateConstants.CONTACT_REGULATOR, "link",
                        NotificationTemplateConstants.HOME_URL, "url")
        );
    }

    @Test
    void notifyInviterAcceptedInvitation() {
        UserInfoDTO invitee = UserInfoDTO.builder()
                .firstName("inviteeName")
                .lastName("inviteeLastName")
                .email("inviteeEmail")
                .build();
        UserInfoDTO inviter =  UserInfoDTO.builder()
                .firstName("inviterName")
                .lastName("inviterLastName")
                .email("inviterEmail")
                .build();

        when(webAppProperties.getUrl()).thenReturn("url");
        when(notificationProperties.getEmail().getContactUsLink()).thenReturn("contactLink");

        // Invoke
        verifierUserNotificationGateway.notifyInviterAcceptedInvitation(invitee, inviter);

        // Verify
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(inviter.getEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();
        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(NotificationTemplateName.INVITER_INVITATION_ACCEPTED);
        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(Map.of(
        		NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_FNAME, inviter.getFirstName(),
        		NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_LNAME, inviter.getLastName(),
        		NotificationTemplateConstants.USER_INVITEE_FNAME, invitee.getFirstName(),
        		NotificationTemplateConstants.USER_INVITEE_LNAME, invitee.getLastName(),
        		NotificationTemplateConstants.CONTACT_REGULATOR, "contactLink",
        		NotificationTemplateConstants.HOME_URL, "url"
        ));
    }

    @Test
    void activatedVerifiers() {
        String userId1 = "verifier1";
        String userId2 = "verifier2";
        List<String> notifications = List.of(userId1, userId2);

        // Invoke
        verifierUserNotificationGateway.notifyUsersUpdateStatus(notifications);

        // Verify
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(userId1, "Verifier");
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(userId2, "Verifier");
        verifyNoMoreInteractions(userNotificationService);
    }

    @Test
    void activatedVerifiers_with_exception() {
        String userId1 = "verifier1";
        String userId2 = "verifier2";
        List<String> notifications = List.of(userId1, userId2);

        doThrow(new RuntimeException())
                .when(userNotificationService)
                .notifyUserAccountActivation(userId1, "Verifier");

        // Invoke
        verifierUserNotificationGateway.notifyUsersUpdateStatus(notifications);

        // Verify
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(userId1, "Verifier");
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(userId2, "Verifier");
        verifyNoMoreInteractions(userNotificationService);
    }

    private VerifierUserInvitationDTO createVerifierUserInvitationDTO() {
        return VerifierUserInvitationDTO.builder()
                .roleCode("roleCode")
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .phoneNumber("69999999999")
                .build();
    }

    private UserNotificationWithRedirectionLinkInfo.TokenParams expectedInvitationLinkTokenParams(String authUuid,
                                                                                                  long expirationInterval) {
        return UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                .jwtTokenAction(JwtTokenAction.VERIFIER_INVITATION)
                .claimValue(authUuid)
                .expirationInterval(expirationInterval)
                .build();
    }

}