package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.restclient.RestClientApi;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompanyInformationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CompanyInformationServiceProperties companyInformationServiceProperties;

    public <T> T getCompanyProfile(String registrationNumber, Class<T> attributesClazz) {
        CompanyProfile companyProfile = performGetCompanyProfileApiCall(registrationNumber);
        return objectMapper.convertValue(companyProfile, attributesClazz);
    }

    private CompanyProfile performGetCompanyProfileApiCall(String registrationNumber) {
        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(companyInformationServiceProperties.getUrl())
                .path(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE.getPath())
                .build(registrationNumber))
            .restEndPoint(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE)
            .headers(buildHttpHeaders())
            .restTemplate(restTemplate)
            .build();


        try {
            ResponseEntity<CompanyProfile> apiResponse = appRestApi.performApiCall();
            return apiResponse.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());

            HttpStatusCode statusCode = e.getStatusCode();
            if (HttpStatus.NOT_FOUND.equals(statusCode)) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, registrationNumber);
            } else {
                throw new BusinessException(ErrorCode.UNAVAILABLE_CH_API, e);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.UNAVAILABLE_CH_API, e);
        }
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(companyInformationServiceProperties.getApiKey(), "");
        return httpHeaders;
    }

}
