package uk.gov.netz.api.workflow.request.flow.common.domain;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.netz.api.restclient.RestClientEndPoint;

/**
 * The enum for common GOV UK rest endpoints.
 */
@Getter
@AllArgsConstructor
public enum RestEndPointEnum implements RestClientEndPoint {

	/** Return information about the UK banking holidays. */
    GOV_UK_GET_BANKING_HOLIDAYS("/bank-holidays.json", HttpMethod.GET, new ParameterizedTypeReference<UkBankHolidays>() {});

    private final String path;
    private final HttpMethod method;
    private final ParameterizedTypeReference<?> parameterizedTypeReference;
}
