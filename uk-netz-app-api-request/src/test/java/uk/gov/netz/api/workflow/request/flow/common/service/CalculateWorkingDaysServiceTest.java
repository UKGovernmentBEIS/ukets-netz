package uk.gov.netz.api.workflow.request.flow.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.restclient.RestClientApi;
import uk.gov.netz.api.workflow.request.flow.common.domain.BankHolidaysForCA;
import uk.gov.netz.api.workflow.request.flow.common.domain.RestEndPointEnum;
import uk.gov.netz.api.workflow.request.flow.common.domain.UkBankHolidays;
import uk.gov.netz.api.workflow.request.flow.common.domain.UkBankHolidaysEvent;

@ExtendWith(MockitoExtension.class)
class CalculateWorkingDaysServiceTest {

	@InjectMocks
    private CalculateWorkingDaysService calculateWorkingDaysService;
	
	@Mock
    private RestTemplate restTemplate; 
	

    @Test
    void calculateWorkingDays() {
    	UkBankHolidays ukBankHolidays = UkBankHolidays.builder()
    			.englandAndWales(BankHolidaysForCA.builder()
    					.events(List.of(UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 1, 6))
    							.build(),
    							UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 1, 13))
    							.build()))
    					.build())
    			.build();
    	
    	LocalDate expectedDate = LocalDate.of(2026, 2, 16);
    	
    	HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString("https://www.gov.uk")
                        .path(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS.getPath())
                        .build()
                        .toUri())
                .restEndPoint(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<UkBankHolidays>() {}))
            .thenReturn(new ResponseEntity<>(ukBankHolidays, HttpStatus.OK));

        // Invoke
        LocalDate actualDate = calculateWorkingDaysService
        		.addWorkingDays(LocalDate.of(2026, 1, 1), 30, CompetentAuthorityEnum.ENGLAND);

        // Verify
        assertThat(actualDate).isEqualTo(expectedDate);
    }
    
    @Test
    void calculateWorkingDays_noHolidays() {
    	UkBankHolidays ukBankHolidays = UkBankHolidays.builder()
    			.englandAndWales(BankHolidaysForCA.builder()
    					.events(List.of(UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 1, 6))
    							.build(),
    							UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 1, 13))
    							.build()))
    					.build())
    			.scotland(BankHolidaysForCA.builder()
    					.events(List.of(UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 5, 30))
    							.build(),
    							UkBankHolidaysEvent
    							.builder()
    							.date(LocalDate.of(2026, 7, 1))
    							.build()))
    					.build())
    			.build();
    	
    	LocalDate expectedDate = LocalDate.of(2026, 2, 12);
    	
    	HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString("https://www.gov.uk")
                        .path(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS.getPath())
                        .build()
                        .toUri())
                .restEndPoint(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<UkBankHolidays>() {}))
            .thenReturn(new ResponseEntity<>(ukBankHolidays, HttpStatus.OK));

        // Invoke
        LocalDate actualDate = calculateWorkingDaysService
        		.addWorkingDays(LocalDate.of(2026, 1, 1), 30, CompetentAuthorityEnum.SCOTLAND);

        // Verify
        assertThat(actualDate).isEqualTo(expectedDate);
    }
    
    @Test
    void calculateWorkingDays_bankHolidaysApiThrowsException() {
    	LocalDate expectedDate = LocalDate.of(2026, 2, 12);
    	
    	HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));

        RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString("https://www.gov.uk")
                        .path(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS.getPath())
                        .build()
                        .toUri())
                .restEndPoint(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<UkBankHolidays>() {}))
        	.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Invoke
        LocalDate actualDate = calculateWorkingDaysService
        		.addWorkingDays(LocalDate.of(2026, 1, 1), 30, CompetentAuthorityEnum.ENGLAND);

        // Verify
        assertThat(actualDate).isEqualTo(expectedDate);
    }
}
