package uk.gov.netz.api.user.core.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.netz.api.restclient.RestClientEndPoint;

import java.util.List;

/**
 * The Pwned passwords rest points enum.
 */
@Getter
@AllArgsConstructor
public enum RestEndPointEnum implements RestClientEndPoint {

    /** Protect the value of the source password being searched for. */
    PWNED_PASSWORDS("/range/{passwordHash}", HttpMethod.GET, new ParameterizedTypeReference<String>() {});

    /** The url. */
    private final String path;

    /** The {@link HttpMethod}. */
    private final HttpMethod method;

    /** The {@link ParameterizedTypeReference}. */
    private final ParameterizedTypeReference<?> parameterizedTypeReference;
}
