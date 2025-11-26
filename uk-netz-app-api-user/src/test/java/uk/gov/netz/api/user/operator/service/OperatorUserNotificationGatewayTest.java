package uk.gov.netz.api.user.operator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.dto.RoleDTO;
import uk.gov.netz.api.authorization.core.service.RoleService;
import uk.gov.netz.api.authorization.operator.domain.NewUserActivated;
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
import uk.gov.netz.api.user.operator.domain.OperatorUserDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserInvitationDTO;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorUserNotificationGatewayTest {

    @InjectMocks
    private OperatorUserNotificationGateway operatorUserNotificationGateway;

    @Mock
    private RoleService roleService;

    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;

    @Mock
    private UserNotificationService userNotificationService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NotificationProperties notificationProperties;

    @Mock
    private JwtProperties jwtProperties;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebAppProperties webAppProperties;


    @Test
    void notifyInvitedUser() {
        String receiverEmail = "receiverEmail";
        String roleCode = "roleCode";
        String accountName = "accountName";
        String authorityUuid = "authorityUuid";
        String roleName = "roleName";
        String helpdesk = "helpdesk";
        RoleDTO roleDTO = RoleDTO.builder().code(roleCode).name(roleName).build();

        OperatorUserInvitationDTO operatorUserInvitationDTO =
            OperatorUserInvitationDTO
                .builder()
                .email(receiverEmail)
                .roleCode(roleCode)
                .build();

        JwtProperties.Claim claim = mock(JwtProperties.Claim.class);
        long expirationInterval = 60L;

        when(roleService.getRoleByCode(roleCode)).thenReturn(roleDTO);
        when(jwtProperties.getClaim()).thenReturn(claim);
        when(notificationProperties.getEmail().getContactUsLink()).thenReturn(helpdesk);

        when(claim.getUserInvitationExpIntervalMinutes()).thenReturn(expirationInterval);

        operatorUserNotificationGateway.notifyInvitedUser(operatorUserInvitationDTO, accountName, authorityUuid);

        verify(roleService , times(1)).getRoleByCode(roleCode);

        ArgumentCaptor<UserNotificationWithRedirectionLinkInfo> notificationInfoCaptor =
            ArgumentCaptor.forClass(UserNotificationWithRedirectionLinkInfo.class);
        verify(userNotificationService, times(1)).notifyUserWithLink(notificationInfoCaptor.capture());

        UserNotificationWithRedirectionLinkInfo notificationInfo = notificationInfoCaptor.getValue();

        assertThat(notificationInfo.getTemplateName()).isEqualTo(NotificationTemplateName.INVITATION_TO_OPERATOR_ACCOUNT);
        assertThat(notificationInfo.getUserEmail()).isEqualTo(operatorUserInvitationDTO.getEmail());
        assertThat(notificationInfo.getLinkParamName()).isEqualTo(NotificationTemplateConstants.OPERATOR_INVITATION_CONFIRMATION_LINK);
        assertThat(notificationInfo.getLinkPath()).isEqualTo(NavigationOutcomes.OPERATOR_REGISTRATION_INVITATION_ACCEPTED_URL);
        assertThat(notificationInfo.getNotificationParams()).containsExactlyInAnyOrderEntriesOf(
            Map.of(
            		NotificationTemplateConstants.USER_ROLE_TYPE, roleDTO.getName(),
                    NotificationTemplateConstants.ACCOUNT_NAME, accountName,
                    NotificationTemplateConstants.EXPIRATION_MINUTES, 60L,
                    NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk
            ));
        assertThat(notificationInfo.getTokenParams())
            .isEqualTo(expectedInvitationLinkTokenParams(JwtTokenAction.OPERATOR_INVITATION, authorityUuid, expirationInterval));

    }

    @Test
    void notifyRegisteredUser() {
        String helpdesk = "helpdesk";
        OperatorUserDTO operatorUserDTO =
            OperatorUserDTO.builder().firstName("fn").lastName("ln").email("email").build();

        NotificationProperties.Email notificationEmail = mock(NotificationProperties.Email.class);
        when(notificationProperties.getEmail()).thenReturn(notificationEmail);
        when(notificationEmail.getContactUsLink()).thenReturn(helpdesk);

        operatorUserNotificationGateway.notifyRegisteredUser(operatorUserDTO);

        //verify
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(operatorUserDTO.getEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();
        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(NotificationTemplateName.USER_ACCOUNT_CREATED);

        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_EMAIL, operatorUserDTO.getEmail(),
                NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk
            ));
    }

    @Test
    void notifyEmailVerification() {
        String email = "email";
        String helpdesk = "helpdesk";
        JwtProperties.Claim claim = mock(JwtProperties.Claim.class);
        long expirationInterval = 10L;

        when(jwtProperties.getClaim()).thenReturn(claim);
        when(claim.getUserInvitationExpIntervalMinutes()).thenReturn(expirationInterval);

        NotificationProperties.Email notificationEmail = mock(NotificationProperties.Email.class);
        when(notificationProperties.getEmail()).thenReturn(notificationEmail);
        when(notificationEmail.getContactUsLink()).thenReturn(helpdesk);

        operatorUserNotificationGateway.notifyEmailVerification("email");

        //verify
        ArgumentCaptor<UserNotificationWithRedirectionLinkInfo> notificationInfoCaptor =
            ArgumentCaptor.forClass(UserNotificationWithRedirectionLinkInfo.class);
        verify(userNotificationService, times(1)).notifyUserWithLink(notificationInfoCaptor.capture());

        UserNotificationWithRedirectionLinkInfo notificationInfo = notificationInfoCaptor.getValue();

        assertThat(notificationInfo.getTemplateName()).isEqualTo(NotificationTemplateName.EMAIL_CONFIRMATION);
        assertThat(notificationInfo.getUserEmail()).isEqualTo(email);
        assertThat(notificationInfo.getLinkParamName()).isEqualTo(NotificationTemplateConstants.EMAIL_CONFIRMATION_LINK);
        assertThat(notificationInfo.getLinkPath()).isEqualTo(NavigationOutcomes.REGISTRATION_EMAIL_VERIFY_CONFIRMATION_URL);
        assertThat(notificationInfo.getNotificationParams())
                .isEqualTo(Map.of(NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk));
        assertThat(notificationInfo.getTokenParams())
            .isEqualTo(expectedInvitationLinkTokenParams(JwtTokenAction.USER_REGISTRATION, email, expirationInterval));
    }

    @Test
    void notifyInviteeAcceptedInvitation() {
    	String helpdesk = "helpdesk";
    	UserInfoDTO inviteeUser = UserInfoDTO.builder()
            .firstName("firstName")
            .lastName("lastName")
            .email("email")
            .build();

        when(notificationProperties.getEmail().getContactUsLink()).thenReturn(helpdesk);
        when(webAppProperties.getUrl()).thenReturn("url");

        operatorUserNotificationGateway.notifyInviteeAcceptedInvitation(inviteeUser);

        //verify
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(inviteeUser.getEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();
        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(NotificationTemplateName.INVITEE_INVITATION_ACCEPTED);
        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(
            Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, "Operator",
                    NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk,
                    NotificationTemplateConstants.HOME_URL, "url")
        );
    }

    @Test
    void notifyInviterAcceptedInvitation() {
    	String helpdesk = "helpdesk";
    	UserInfoDTO inviteeUser = UserInfoDTO.builder()
                .firstName("inviteeName")
                .lastName("inviteeLastName")
                .email("inviteeEmail")
                .build();
        UserInfoDTO inviterUser =  UserInfoDTO.builder()
                .firstName("inviterName")
                .lastName("inviterLastName")
                .email("inviterEmail")
                .build();

        when(notificationProperties.getEmail().getContactUsLink()).thenReturn(helpdesk);
        when(webAppProperties.getUrl()).thenReturn("url");
        
        operatorUserNotificationGateway.notifyInviterAcceptedInvitation(inviteeUser, inviterUser);

        //verify
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailInfoCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(notificationEmailService, times(1)).notifyRecipient(emailInfoCaptor.capture(), Mockito.eq(inviterUser.getEmail()));

        EmailData<EmailNotificationTemplateData> emailInfo = emailInfoCaptor.getValue();
        assertThat(emailInfo.getNotificationTemplateData().getTemplateName()).isEqualTo(NotificationTemplateName.INVITER_INVITATION_ACCEPTED);
        assertThat(emailInfo.getNotificationTemplateData().getTemplateParams()).containsExactlyInAnyOrderEntriesOf(Map.of(
                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_FNAME, inviterUser.getFirstName(),
                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_LNAME, inviterUser.getLastName(),
                NotificationTemplateConstants.USER_INVITEE_FNAME, inviteeUser.getFirstName(),
                NotificationTemplateConstants.USER_INVITEE_LNAME, inviteeUser.getLastName(),
                NotificationTemplateConstants.CONTACT_REGULATOR, helpdesk,
                NotificationTemplateConstants.HOME_URL, "url"
        ));
    }

    @Test
    void notifyUsersUpdateStatus() {
        Long accountId = 1L;
        String installationName = "installationName";
        String roleName = "roleName";
        RoleDTO roleDTO = RoleDTO.builder().code(AuthorityConstants.OPERATOR_ROLE_CODE).name(roleName).build();

        NewUserActivated operator1 = NewUserActivated.builder().userId("operator1").roleCode(AuthorityConstants.OPERATOR_ROLE_CODE).build();
        NewUserActivated operator2 = NewUserActivated.builder().userId("operator2").roleCode(AuthorityConstants.OPERATOR_ROLE_CODE).build();
        NewUserActivated emitter1 = NewUserActivated.builder().userId("emitter1").accountId(accountId)
                .roleCode(AuthorityConstants.EMITTER_CONTACT).build();
        NewUserActivated emitter2 = NewUserActivated.builder().userId("emitter2").accountId(accountId)
                .roleCode(AuthorityConstants.EMITTER_CONTACT).build();

        List<NewUserActivated> activatedOperators = List.of(operator1, operator2, emitter1, emitter2);

        when(accountQueryService.getAccountName(accountId)).thenReturn(installationName);
        when(roleService.getRoleByCode(AuthorityConstants.OPERATOR_ROLE_CODE)).thenReturn(roleDTO);

        // Invoke
        operatorUserNotificationGateway.notifyUsersUpdateStatus(activatedOperators);

        // Verify
        verify(userNotificationService, times(1))
                .notifyEmitterContactAccountActivation(emitter1.getUserId(), installationName);
        verify(userNotificationService, times(1))
                .notifyEmitterContactAccountActivation(emitter2.getUserId(), installationName);
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(operator1.getUserId(), roleDTO.getName());
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(operator2.getUserId(), roleDTO.getName());
        verify(roleService, times(2)).getRoleByCode(roleDTO.getCode());
        verify(accountQueryService, times(2))
                .getAccountName(accountId);
        verifyNoMoreInteractions(userNotificationService, accountQueryService);
    }

    @Test
    void notifyUsersUpdateStatus_with_exception() {
        Long accountId = 1L;
        String installationName = "installationName";
        String roleName = "roleName";
        RoleDTO roleDTO = RoleDTO.builder().code(AuthorityConstants.OPERATOR_ROLE_CODE).name(roleName).build();

        NewUserActivated operator1 = NewUserActivated.builder().userId("operator1").roleCode(AuthorityConstants.OPERATOR_ROLE_CODE).build();
        NewUserActivated operator2 = NewUserActivated.builder().userId("operator2").roleCode(AuthorityConstants.OPERATOR_ROLE_CODE).build();
        NewUserActivated emitter1 = NewUserActivated.builder().userId("emitter1").accountId(accountId)
                .roleCode(AuthorityConstants.EMITTER_CONTACT).build();

        List<NewUserActivated> activatedOperators = List.of(emitter1, operator1, operator2);

        when(accountQueryService.getAccountName(accountId))
                .thenThrow(NullPointerException.class);
        when(roleService.getRoleByCode(AuthorityConstants.OPERATOR_ROLE_CODE)).thenReturn(roleDTO);

        // Invoke
        operatorUserNotificationGateway.notifyUsersUpdateStatus(activatedOperators);

        // Verify
        verify(userNotificationService, never())
                .notifyEmitterContactAccountActivation(emitter1.getUserId(), installationName);
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(operator1.getUserId(), roleDTO.getName());
        verify(userNotificationService, times(1))
                .notifyUserAccountActivation(operator2.getUserId(), roleDTO.getName());
        verify(roleService, times(2)).getRoleByCode(roleDTO.getCode());
        verify(accountQueryService, times(1))
                .getAccountName(accountId);
        verifyNoMoreInteractions(userNotificationService, accountQueryService);
    }

    private UserNotificationWithRedirectionLinkInfo.TokenParams expectedInvitationLinkTokenParams(JwtTokenAction jwtTokenAction,
                                                                                                  String claimValue, long expirationInterval) {
        return UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
            .jwtTokenAction(jwtTokenAction)
            .claimValue(claimValue)
            .expirationInterval(expirationInterval)
            .build();
    }
}