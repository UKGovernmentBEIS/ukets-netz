package uk.gov.netz.api.referencedata.service;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.referencedata.domain.Country;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UKCountryValidatorTest {

	@InjectMocks
	private UKCountryValidator ukCountryValidator;

	@Mock
	private CountryService countryService;

	@Test
	void testInvalidCountry() {
		List<Country> countries = buildCountries("GB-ENG", "GB-NIR", "GB-SCT", "GB-WLS");
		when(countryService.getUKReferenceData())
				.thenReturn(countries);

		assertFalse(ukCountryValidator.isValid("INVALID_COUNTRY_CODE", Mockito.mock(ConstraintValidatorContext.class)));
	}

	@Test
	void testValidCountry() {
		List<Country> countries = buildCountries("GB-ENG", "GB-NIR", "GB-SCT", "GB-WLS");
		when(countryService.getUKReferenceData()).thenReturn(countries);

		assertTrue(ukCountryValidator.isValid("GB-ENG", Mockito.mock(ConstraintValidatorContext.class)));
	}

	private List<Country> buildCountries(String... countryCodes) {
		return Arrays.stream(countryCodes).map(countryCode -> Country.builder()
						.code(countryCode)
						.name(countryCode + "_name")
						.officialName(countryCode + "_official").build())
				.collect(Collectors.toList());
	}
}