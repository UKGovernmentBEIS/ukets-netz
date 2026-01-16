package uk.gov.netz.api.restclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

public interface RestClientEndPoint<T> {

    String getPath();
    HttpMethod getMethod();
    ParameterizedTypeReference getParameterizedTypeReference();
}
