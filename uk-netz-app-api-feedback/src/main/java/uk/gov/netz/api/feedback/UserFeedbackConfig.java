package uk.gov.netz.api.feedback;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "user-feedback")
@Getter
@Setter
public class UserFeedbackConfig {

    private List<String> recipients = new ArrayList<>();

}
