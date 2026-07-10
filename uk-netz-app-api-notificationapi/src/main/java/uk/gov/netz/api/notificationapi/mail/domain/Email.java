package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Object that holds the appropriate information in order to send an email.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Email {

    private String from;
    private EmailRecipients recipients;

    private String subject;
    private String text;

    /**
     * key: attachment name
     * value: the file content
     */
    @Builder.Default
    private Map<String, byte[]> attachments = new HashMap<>();
}
