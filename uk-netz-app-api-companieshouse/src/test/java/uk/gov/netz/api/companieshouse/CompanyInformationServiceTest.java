package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.restclient.RestClientApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyInformationServiceTest {

    @InjectMocks
    private CompanyInformationService companyInformationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CompanyInformationServiceProperties companyInformationServiceProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getCompanyProfile() {
        String registrationNumber = "registrationNumber";
        String apiKey = "key";
        String url = "http://www.google.gr/";

        CompanyProfile companyProfile = CompanyProfile.builder()
            .name("name")
            .registrationNumber(registrationNumber)
            .status("active")
            .address(CompanyAddress.builder()
                .city("someCity").line1("addr-line1").line2("addr-line2").country("GR").county("region").postcode("12345").build())
            .sicCodes(List.of(SicCode.builder().code("12345").build())).build();

        CompanyProfileDTO expectedCompanyProfileDTO = CompanyProfileDTO.builder()
            .name("name")
            .registrationNumber(registrationNumber)
            .address(CountyAddressDTO.builder()
                .city("someCity").line1("addr-line1").line2("addr-line2").county("region").postcode("12345").build())
            .sicCodes(List.of(SicCode.builder().code("12345").build()))
            .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(apiKey, "");

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString(url)
                        .path(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE.getPath())
                        .build(registrationNumber))
                .restEndPoint(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(companyInformationServiceProperties.getApiKey()).thenReturn(apiKey);
        when(companyInformationServiceProperties.getUrl()).thenReturn(url);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<CompanyProfile>() {}))
            .thenReturn(new ResponseEntity<>(companyProfile, HttpStatus.OK));

        // Invoke
        CompanyProfileDTO actualCompanyProfileDTO = companyInformationService.getCompanyProfile(registrationNumber, CompanyProfileDTO.class);

        // Verify
        assertEquals(expectedCompanyProfileDTO, actualCompanyProfileDTO);
    }

    @Test
    void getCompanyProfile_throws_exception_when_company_not_found() {
        String registrationNumber = "registrationNumber";
        String apiKey = "key";
        String url = "http://www.google.gr/";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(apiKey, "");

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString(url)
                        .path(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE.getPath())
                        .build(registrationNumber))
                .restEndPoint(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(companyInformationServiceProperties.getApiKey()).thenReturn(apiKey);
        when(companyInformationServiceProperties.getUrl()).thenReturn(url);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<CompanyProfile>() {}))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
            () -> companyInformationService.getCompanyProfile(registrationNumber, CompanyProfileDTO.class));

        // Verify
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
    }

    @Test
    void getCompanyProfile_throws_exception() {
        String registrationNumber = "registrationNumber";
        String apiKey = "key";
        String url = "http://www.google.gr/";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(apiKey, "");

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString(url)
                        .path(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE.getPath())
                        .build(registrationNumber))
                .restEndPoint(CompanyInformationRestEndPointEnum.GET_COMPANY_PROFILE)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(companyInformationServiceProperties.getApiKey()).thenReturn(apiKey);
        when(companyInformationServiceProperties.getUrl()).thenReturn(url);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<CompanyProfile>() {}))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
            () -> companyInformationService.getCompanyProfile(registrationNumber, CompanyProfileDTO.class));

        // Verify
        assertEquals(ErrorCode.UNAVAILABLE_CH_API, businessException.getErrorCode());
    }
}