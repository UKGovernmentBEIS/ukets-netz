package uk.gov.netz.api.restclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final RestClientProperties clientProperties;

    public RestClientConfig(RestClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

	@Bean
	RestTemplate restTemplate(RestTemplateBuilder builder,
			RestClientCorrelationHeaderRequestInterceptor restClientCorrelationHeaderRequestInterceptor,
			RestClientCorrelationParentHeaderRequestInterceptor restClientCorrelationParentHeaderRequestInterceptor,
			RestClientLoggingInterceptor loggingInterceptor) {
		return builder.setConnectTimeout(Duration.ofMillis(clientProperties.getConnectTimeout()))
				.setReadTimeout(Duration.ofMillis(clientProperties.getReadTimeout()))
				//sequence matters. correlation header should be executed first
				.additionalInterceptors(restClientCorrelationHeaderRequestInterceptor,
						restClientCorrelationParentHeaderRequestInterceptor, 
						loggingInterceptor)
				.build();
	}

}
