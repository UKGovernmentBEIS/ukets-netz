package uk.gov.netz.api.user.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.user.NotificationTemplateConstants;
import uk.gov.netz.api.token.JwtProperties;
import uk.gov.netz.api.token.JwtTokenAction;
import uk.gov.netz.api.token.JwtTokenService;
import uk.gov.netz.api.user.NavigationOutcomes;
import uk.gov.netz.api.user.NotificationTemplateName;
import uk.gov.netz.api.user.core.domain.dto.ResetPasswordDTO;
import uk.gov.netz.api.user.core.domain.model.UserNotificationWithRedirectionLinkInfo;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserResetPasswordService {

    private final UserNotificationService userNotificationService;
    private final UserAuthService userAuthService;
    private final JwtProperties jwtProperties;
    private final NotificationProperties notificationProperties;
    private final JwtTokenService jwtTokenService;

    public void sendResetPasswordEmail(String email) {
        long expirationInMinutes = jwtProperties.getClaim().getResetPasswordExpIntervalMinutes();
        Optional<UserInfoDTO> user = userAuthService.getUserByEmail(email);

        if (user.isPresent()) {
            Map<String, Object> notificationParams = new HashMap<>(Map.of(
                    NotificationTemplateConstants.CONTACT_REGULATOR, notificationProperties.getEmail().getContactUsLink(),
                    NotificationTemplateConstants.EXPIRATION_MINUTES, expirationInMinutes
            ));

            userNotificationService.notifyUserWithLink(
                    UserNotificationWithRedirectionLinkInfo.builder()
                            .templateName(NotificationTemplateName.RESET_PASSWORD_REQUEST)
                            .userEmail(email)
                            .notificationParams(notificationParams)
                            .linkParamName(NotificationTemplateConstants.RESET_PASSWORD_LINK)
                            .linkPath(NavigationOutcomes.RESET_PASSWORD_URL)
                            .tokenParams(UserNotificationWithRedirectionLinkInfo.TokenParams.builder()
                                    .jwtTokenAction(JwtTokenAction.RESET_PASSWORD)
                                    .claimValue(email)
                                    .expirationInterval(jwtProperties.getClaim().getResetPasswordExpIntervalMinutes())
                                    .build()
                            )
                            .build()
            );
        }
    }

    public String verifyToken(String token) {
        return jwtTokenService.resolveTokenActionClaim(token, JwtTokenAction.RESET_PASSWORD);
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String email = verifyToken(resetPasswordDTO.getToken());
        userAuthService.resetPassword(
                email, resetPasswordDTO.getOtp(), resetPasswordDTO.getPassword());

        Optional<UserInfoDTO> user = userAuthService.getUserByEmail(email);
        user.ifPresent(userInfoDTO -> userNotificationService.notifyUserPasswordReset(userInfoDTO.getUserId()));
    }

}
