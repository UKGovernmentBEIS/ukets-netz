package uk.gov.netz.api.companieshouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.netz.api.restclient.RestClientEndPoint;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

@Getter
@AllArgsConstructor
public enum CompanyInformationRestEndPointEnum implements RestClientEndPoint {

    GET_COMPANY_PROFILE("/company/{companyNumber}", HttpMethod.GET, new ParameterizedTypeReference<CompanyProfile>() {});

    private final String path;
    private final HttpMethod method;
    private final ParameterizedTypeReference<?> parameterizedTypeReference;

}
