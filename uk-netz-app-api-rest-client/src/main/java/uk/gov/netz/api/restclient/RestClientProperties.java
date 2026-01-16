package uk.gov.netz.api.restclient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "client")
@Getter
@Setter
public class RestClientProperties {
	private Integer connectTimeout = 10000;
	private Integer readTimeout = 10000;
}
