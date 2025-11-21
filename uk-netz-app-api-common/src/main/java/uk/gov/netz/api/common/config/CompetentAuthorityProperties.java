package uk.gov.netz.api.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "competent-authority")
@Getter
@Setter
public class CompetentAuthorityProperties {

	@Valid
	@NotEmpty
	private String centralInfo;
}
