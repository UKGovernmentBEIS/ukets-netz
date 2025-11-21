package uk.gov.netz.api.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web")
@Getter
@Setter
public class WebAppProperties {
	@Valid
	@NotEmpty @URL
	private String url;
}
