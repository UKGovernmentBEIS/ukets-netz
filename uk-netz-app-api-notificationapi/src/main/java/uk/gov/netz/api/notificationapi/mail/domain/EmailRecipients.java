package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class EmailRecipients {

    @Builder.Default
    private List<String> to = new ArrayList<>();

    @Builder.Default
    private List<String> cc = new ArrayList<>();
    
    @Builder.Default
    private List<String> bcc = new ArrayList<>();
    
}
