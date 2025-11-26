package uk.gov.netz.api.user.verifier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class VerifierUserNotificationGateway {

    private final UserNotificationService userNotificationService;
    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    private final JwtProperties jwtProperties;
    private final NotificationProperties notificationProperties;
    private final WebAppProperties webAppProperties;

    /**
     * Notifies by email the user that an invitation to participate in a verification body is awaiting to be confirmed.
     * @param verifierUserInvitation the {@link VerifierUserInvitationDTO} containing the invited user's info
     * @param authorityUuid the authority uuid
     */
    public void notifyInvitedUser(VerifierUserInvitationDTO verifierUserInvitation, String authorityUuid) {
        long expirationInMinutes = jwtProperties.getClaim().getUserInvitationExpIntervalMinutes();

        Map<String, Object> notificationParams = new HashMap<>(Map.of(
                NotificationTemplateConstants.APPLICANT_FNAME, verifierUserInvitation.getFirstName(),
                NotificationTemplateConstants.APPLICANT_LNAME, verifierUserInvitation.getLastName(),
                NotificationTemplateConstants.EXPIRATION_MINUTES, expirationInMinutes,
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()
        ));

        userNotificationService.notifyUserWithLink(
                UserNotificationWithRedirectionLinkInfo.builder()
                        .templateName(NotificationTemplateName.INVITATION_TO_VERIFIER_ACCOUNT)
                        .userEmail(verifierUserInvitation.getEmail())
                        .notificationParams(notificationParams)
                        .linkParamName(NotificationTemplateConstants.VERIFIER_INVITATION_CONFIRMATION_LINK)
                        .linkPath(NavigationOutcomes.VERIFIER_REGISTRATION_INVITATION_ACCEPTED_URL)
                        .tokenParams(UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                                .jwtTokenAction(JwtTokenAction.VERIFIER_INVITATION)
                                .claimValue(authorityUuid)
                                .expirationInterval(expirationInMinutes)
                                .build()
                        )
                        .build()
        );
    }

    public void notifyInviteeAcceptedInvitation(UserInfoDTO invitee) {
        EmailData<EmailNotificationTemplateData> inviteeInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.INVITEE_INVITATION_ACCEPTED)
                        .templateParams(Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, "Verifier",
                                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl()))
                        .build())
                .build();

        notificationEmailService.notifyRecipient(inviteeInfo, invitee.getEmail());
    }

    public void notifyInviterAcceptedInvitation(UserInfoDTO invitee, UserInfoDTO inviter) {
        EmailData<EmailNotificationTemplateData> inviteeInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.INVITER_INVITATION_ACCEPTED)
                        .templateParams(Map.of(NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_FNAME, inviter.getFirstName(),
                        		NotificationTemplateConstants.USER_ACCOUNT_CREATED_USER_LNAME, inviter.getLastName(),
                        		NotificationTemplateConstants.USER_INVITEE_FNAME, invitee.getFirstName(),
                        		NotificationTemplateConstants.USER_INVITEE_LNAME, invitee.getLastName(),
                                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl()))
                        .build())
                
                .build();

        notificationEmailService.notifyRecipient(inviteeInfo, inviter.getEmail());
    }

    public void notifyUsersUpdateStatus(List<String> activatedVerifiers) {
        activatedVerifiers.forEach(user -> {
            try {
                userNotificationService.notifyUserAccountActivation(user, "Verifier");
            } catch (Exception ex) {
                log.error("Exception during sending email for update verifier status:", ex);
            }
        });
    }
}
