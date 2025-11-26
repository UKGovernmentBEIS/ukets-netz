package uk.gov.netz.api.workflow.request.flow.common.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.restclient.RestClientApi;
import uk.gov.netz.api.workflow.request.flow.common.domain.BankHolidaysForCA;
import uk.gov.netz.api.workflow.request.flow.common.domain.RestEndPointEnum;
import uk.gov.netz.api.workflow.request.flow.common.domain.UkBankHolidays;

@Log4j2
@Service
@RequiredArgsConstructor
public class CalculateWorkingDaysService {

	private final RestTemplate restTemplate;
	private final Set<DayOfWeek> WEEKEND = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
	 
	/**
     * Add specified number of working days (ie excluding weekends and bank holidays) to the given date.
     */
    public LocalDate addWorkingDays(LocalDate date, int workingDays, CompetentAuthorityEnum ca) {
    	List<LocalDate> bankHolidays = getBankHolidaysByCA(date, ca);
        int addedDays = 0;
        while (addedDays < workingDays) {
            date = date.plusDays(1);
            if (!WEEKEND.contains(date.getDayOfWeek()) && !bankHolidays.contains(date)) {
                ++addedDays;
            }
        }
        return date;
    }
	
	/**
     * Retrieve all the bank holidays for the specified CA.
     */
	public List<LocalDate> getBankHolidaysByCA(LocalDate date, CompetentAuthorityEnum ca) {
		
		UkBankHolidays ukBankHolidays = performGetUkBankHolidaysApiCall();
		
		return Optional.ofNullable(ukBankHolidays)
				.map(holidays -> getBankHolidaysForCA(ca, holidays))
				.map(caHoliday -> Optional.ofNullable(caHoliday.getEvents())
						.map(Collection::stream)
						.orElseGet(Stream::empty)
						.filter(event -> event.getDate().isAfter(date))
						.map(event -> event.getDate())
						.collect(Collectors.toList()))
				.orElse(List.of());
	}

	/**
     * Retrieve the appropriate data based on the provided CA. 
     */
	private BankHolidaysForCA getBankHolidaysForCA(CompetentAuthorityEnum ca, UkBankHolidays ukBankHolidays) {
		return switch (ca) {
			case SCOTLAND -> ukBankHolidays.getScotland();
			case NORTHERN_IRELAND -> ukBankHolidays.getNorthernIreland();
			default -> ukBankHolidays.getEnglandAndWales();
			};
	}

	/**
     * Retrieve the UK bank holidays from  <a href="https://www.gov.uk/bank-holidays.json"></a>
     */
	private UkBankHolidays performGetUkBankHolidaysApiCall() {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        
		final RestClientApi appRestApi = RestClientApi.builder()
                .uri(UriComponentsBuilder
                        .fromUriString("https://www.gov.uk")
                        .path(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS.getPath())
                        .build()
                        .toUri())
                .restEndPoint(RestEndPointEnum.GOV_UK_GET_BANKING_HOLIDAYS)
                .headers(httpHeaders)
                .restTemplate(restTemplate)
                .build();

        try {
        	ResponseEntity<UkBankHolidays> apiResponse = appRestApi.performApiCall();
            return apiResponse.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            return null;
        }
	}
}
