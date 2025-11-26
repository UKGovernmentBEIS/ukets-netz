package uk.gov.netz.api.user.operator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class OperatorUserNotificationGateway {

    private final RoleService roleService;
    private final AccountQueryService accountQueryService;
    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    private final UserNotificationService userNotificationService;
    private final NotificationProperties notificationProperties;
    private final JwtProperties jwtProperties;
    private final WebAppProperties webAppProperties;

    /**
     * Sends an {@link NotificationTemplateName#INVITATION_TO_OPERATOR_ACCOUNT} email with receiver email param as recipient.
     * @param operatorUserInvitationDTO the invited operator user to notify
     * @param accountName the account name that will be used to form the email body
     * @param authorityUuid the uuid that will be used to form the token that will be send with the email body
     */
    public void notifyInvitedUser(OperatorUserInvitationDTO operatorUserInvitationDTO, String accountName,
                                  String authorityUuid) {
        RoleDTO roleDTO = roleService.getRoleByCode(operatorUserInvitationDTO.getRoleCode());
        long expirationInMinutes = jwtProperties.getClaim().getUserInvitationExpIntervalMinutes();

        Map<String, Object> notificationParams = new HashMap<>(Map.of(
                NotificationTemplateConstants.USER_ROLE_TYPE, roleDTO.getName(),
                NotificationTemplateConstants.ACCOUNT_NAME, accountName,
                NotificationTemplateConstants.EXPIRATION_MINUTES, expirationInMinutes,
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()
        ));

        userNotificationService.notifyUserWithLink(
                UserNotificationWithRedirectionLinkInfo.builder()
                        .templateName(NotificationTemplateName.INVITATION_TO_OPERATOR_ACCOUNT)
                        .userEmail(operatorUserInvitationDTO.getEmail())
                        .notificationParams(notificationParams)
                        .linkParamName(NotificationTemplateConstants.OPERATOR_INVITATION_CONFIRMATION_LINK)
                        .linkPath(NavigationOutcomes.OPERATOR_REGISTRATION_INVITATION_ACCEPTED_URL)
                        .tokenParams(UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                                .jwtTokenAction(JwtTokenAction.OPERATOR_INVITATION)
                                .claimValue(authorityUuid)
                                .expirationInterval(expirationInMinutes)
                                .build()
                        )
                        .build()
        );
    }

    public void notifyRegisteredUser(OperatorUserDTO operatorUserDTO) {
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.USER_ACCOUNT_CREATED)
                        .templateParams(Map.of(
                                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_EMAIL, operatorUserDTO.getEmail(),
                                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()))
                        .build())
                .build();

        notificationEmailService.notifyRecipient(emailInfo, operatorUserDTO.getEmail());
    }

    public void notifyEmailVerification(String email) {
        Map<String, Object> notificationParams = new HashMap<>();
        notificationParams.put(NotificationTemplateConstants.CONTACT_REGULATOR,
                notificationProperties.getEmail().getContactUsLink());

        userNotificationService.notifyUserWithLink(
                UserNotificationWithRedirectionLinkInfo.builder()
                        .templateName(NotificationTemplateName.EMAIL_CONFIRMATION)
                        .notificationParams(notificationParams)
                        .userEmail(email)
                        .linkParamName(NotificationTemplateConstants.EMAIL_CONFIRMATION_LINK)
                        .linkPath(NavigationOutcomes.REGISTRATION_EMAIL_VERIFY_CONFIRMATION_URL)
                        .tokenParams(UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                                .jwtTokenAction(JwtTokenAction.USER_REGISTRATION)
                                .claimValue(email)
                                .expirationInterval(jwtProperties.getClaim().getUserInvitationExpIntervalMinutes())
                                .build()
                        )
                        .build()
        );
    }

    public void notifyInviteeAcceptedInvitation(UserInfoDTO inviteeUser) {
        EmailData<EmailNotificationTemplateData> inviteeInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.INVITEE_INVITATION_ACCEPTED)
                        .templateParams(Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, "Operator",
                                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl()))
                        .build())
                .build();

        notificationEmailService.notifyRecipient(inviteeInfo, inviteeUser.getEmail());
    }

    public void notifyInviterAcceptedInvitation(UserInfoDTO inviteeUser, UserInfoDTO inviterUser) {
        EmailData<EmailNotificationTemplateData> inviteeInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.INVITER_INVITATION_ACCEPTED)
                        .templateParams(Map.of(
                                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_FNAME, inviterUser.getFirstName(),
                                NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_LNAME, inviterUser.getLastName(),
                                NotificationTemplateConstants.USER_INVITEE_FNAME, inviteeUser.getFirstName(),
                                NotificationTemplateConstants.USER_INVITEE_LNAME, inviteeUser.getLastName(),
                                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl()))
                        .build())
                .build();

        notificationEmailService.notifyRecipient(inviteeInfo, inviterUser.getEmail());
    }

    public void notifyUsersUpdateStatus(List<NewUserActivated> activatedOperators) {
        activatedOperators.forEach(user -> {
            try {
                if (AuthorityConstants.EMITTER_CONTACT.equals(user.getRoleCode())) {
                    String installationName = accountQueryService.getAccountName(user.getAccountId());
                    userNotificationService.notifyEmitterContactAccountActivation(user.getUserId(), installationName);
                }
                else {
                    RoleDTO roleDTO = roleService.getRoleByCode(user.getRoleCode());
                    userNotificationService.notifyUserAccountActivation(user.getUserId(), roleDTO.getName());
                }
            } catch (Exception ex) {
                log.error("Exception during sending email for update operator status:", ex);
            }
        });
    }
}