package uk.gov.netz.api.notificationapi.mail.config.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class NotificationProperties {

    @Valid
    private Email email;
    
    @Valid
    private SmtpHeaders smtpHeaders;

    @Getter
    @Setter
    public static class Email {

        @Valid
        @NotEmpty
        private String autoSender;

        @Valid
        @NotEmpty
        private String contactUsLink;
    }
    
    @Getter
    @Setter
    public static class SmtpHeaders {

    	@NotEmpty
        private String emailOriginator;

    }
}
