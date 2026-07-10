package uk.gov.netz.api.token;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    @Valid
    private Claim claim;

    @Getter
    @Setter
    public static class Claim {

        @Valid
        @NotEmpty
        private String audience;

        @Valid
        @NotEmpty
        private String issuer;

        @Valid
        @NotEmpty
        private String secret;

        private long userInvitationExpIntervalMinutes = 4320L;

        private long change2faExpIntervalMinutes = 5L;
        
        private long getFileAttachmentExpIntervalMinutes = 1L;
        
        private long resetPasswordExpIntervalMinutes = 20L;
    }


}
