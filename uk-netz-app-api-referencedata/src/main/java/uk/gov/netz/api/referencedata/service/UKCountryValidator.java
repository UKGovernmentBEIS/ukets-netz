package uk.gov.netz.api.referencedata.service;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

/**
 * The UK country validator (validates against the country code (e.g. GB-ENG))
 *
 */
public class UKCountryValidator implements ConstraintValidator<UKCountry, String> {

	private final CountryService countryService;

	public UKCountryValidator() {
		this.countryService = null;
	}

	@Autowired
	public UKCountryValidator(CountryService countryService) {
		this.countryService = countryService;
	}

	@Override
	public boolean isValid(String countryCode, ConstraintValidatorContext context) {
		if (countryService != null && !ObjectUtils.isEmpty(countryCode)) {
			return countryService.getUKReferenceData().stream().anyMatch(country -> country.getCode().equals(countryCode));
		}
		return true;
	}
}
