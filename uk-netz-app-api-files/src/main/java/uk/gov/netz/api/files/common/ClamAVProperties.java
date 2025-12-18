package uk.gov.netz.api.files.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "clamav")
@Getter
@Setter
public class ClamAVProperties {

	@Valid
	@NotEmpty
	private String host;

	@Valid
	@NotNull
	private Integer port;
}
