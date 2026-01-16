package uk.gov.netz.api.restclient;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Data
@Builder
@Log4j2
public class RestClientApi {

    private URI uri;
    private RestClientEndPoint restEndPoint;
    private HttpHeaders headers;
    private RestTemplate restTemplate;
    private Object body;

    public <T> ResponseEntity<T> performApiCall() {

        //https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-uri-building.html#uri-encoding
        HttpEntity<Object> requestEntity = this.body != null ? new HttpEntity<>(this.body, this.headers) : new HttpEntity<>(this.headers);
        try {
            return this.restTemplate.exchange(uri, this.restEndPoint.getMethod(), requestEntity, this.restEndPoint.getParameterizedTypeReference());
        } catch (Exception ex) {
            log.error("Failed to invoke External API Rest with url '{}'", () -> uri);
            throw ex;
        }
    }
}
