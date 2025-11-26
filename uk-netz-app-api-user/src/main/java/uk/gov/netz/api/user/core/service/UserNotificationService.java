package uk.gov.netz.api.user.core.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.user.NotificationTemplateConstants;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.token.JwtTokenService;
import uk.gov.netz.api.user.NavigationParams;
import uk.gov.netz.api.user.NotificationTemplateName;
import uk.gov.netz.api.user.core.domain.model.UserNotificationWithRedirectionLinkInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserInfoApi userInfoApi;
    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    private final WebAppProperties webAppProperties;
    private final NotificationProperties notificationProperties;
    private final JwtTokenService jwtTokenService;

    /**
     * Sends email notification containing a redirection link to user.
     * @param notificationInfo {@link UserNotificationWithRedirectionLinkInfo}
     */
    public void notifyUserWithLink(UserNotificationWithRedirectionLinkInfo notificationInfo) {
        String redirectionLink = constructRedirectionLink(notificationInfo.getLinkPath(), notificationInfo.getTokenParams());

        Map<String, Object> notificationParameters = !ObjectUtils.isEmpty(notificationInfo.getNotificationParams()) ?
            notificationInfo.getNotificationParams() :
            new HashMap<>();

        notificationParameters.put(notificationInfo.getLinkParamName(), redirectionLink);

        notifyUser(notificationInfo.getUserEmail(), notificationInfo.getTemplateName(), notificationParameters);
    }

    public void notifyUserAccountActivation(String userId, String roleName) {
        UserInfoDTO user = userInfoApi.getUserByUserId(userId);
        
        notifyUser(user.getEmail(), NotificationTemplateName.USER_ACCOUNT_ACTIVATION, Map.of(NotificationTemplateConstants.USER_ROLE_TYPE, roleName,
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl()));
    }

    public void notifyEmitterContactAccountActivation(String userId, String installationName) {
        UserInfoDTO user = userInfoApi.getUserByUserId(userId);
        
        notifyUser(user.getEmail(), NotificationTemplateName.INVITATION_TO_EMITTER_CONTACT, Map.of(NotificationTemplateConstants.APPLICANT_FNAME, user.getFirstName(),
        		NotificationTemplateConstants.APPLICANT_LNAME, user.getLastName(),
                NotificationTemplateConstants.ACCOUNT_NAME, installationName,
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()));
    }
    
    public void notifyUserPasswordReset(String userId) {
        UserInfoDTO user = userInfoApi.getUserByUserId(userId);
        
        notifyUser(user.getEmail(), NotificationTemplateName.RESET_PASSWORD_CONFIRMATION, Map.of(
                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl(),
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()));
    }
    
    public void notifyUserReset2Fa(String userId) {
        UserInfoDTO user = userInfoApi.getUserByUserId(userId);
        
        notifyUser(user.getEmail(), NotificationTemplateName.RESET_2FA_CONFIRMATION, Map.of(
                NotificationTemplateConstants.HOME_URL, webAppProperties.getUrl(),
                NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink()));
    }
    
    private void notifyUser(String email, String templateName, Map<String, Object> params) {
        EmailData<EmailNotificationTemplateData> emailInfo = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .templateName(templateName)
                        .templateParams(params)
                        .build())
                .build();

        notificationEmailService.notifyRecipient(emailInfo, email);
    }

    private String constructRedirectionLink(String path, UserNotificationWithRedirectionLinkInfo.TokenParams tokenParams) {
        String token = jwtTokenService
            .generateToken(tokenParams.getJwtTokenAction(), tokenParams.getClaimValue(), tokenParams.getExpirationInterval());

        return UriComponentsBuilder
            .fromUriString(webAppProperties.getUrl())
            .path("/")
            .path(path)
            .queryParam(NavigationParams.TOKEN, token)
            .build()
            .toUriString();
    }	
}
