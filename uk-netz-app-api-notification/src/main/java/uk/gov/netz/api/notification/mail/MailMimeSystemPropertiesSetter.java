package uk.gov.netz.api.notification.mail;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MailMimeSystemPropertiesSetter {

    @PostConstruct
    public void setProperties() {
        System.setProperty("mail.mime.splitlongparameters", "false");
    }
}
