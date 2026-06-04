package uk.gov.netz.api.referencedata.service;

import org.springframework.stereotype.Service;
import uk.gov.netz.api.referencedata.domain.Country;
import uk.gov.netz.api.referencedata.domain.constants.CountryConstants;
import uk.gov.netz.api.referencedata.repository.CountryRepository;

import java.util.List;

@Service("countryService")
public class CountryService implements ReferenceDataService<Country> {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Country> getReferenceData() {
        return countryRepository.findAll();
    }

	public List<Country> getUKReferenceData() {
		return countryRepository.findAllByCodeIn(CountryConstants.getUKCountryCodes());
	}
}
