package uk.gov.netz.api.thirdpartydataprovider.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.repository.ThirdPartyDataProviderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThirdPartyDataProviderQueryService {

	private final ThirdPartyDataProviderRepository thirdPartyDataProviderRepository;

    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name){
        return thirdPartyDataProviderRepository.existsByNameIgnoreCase(name);
    }

	@Transactional(readOnly = true)
	public boolean existsById(Long id){
		return thirdPartyDataProviderRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public List<ThirdPartyDataProviderNameInfoDTO> getAllThirdPartyDataProviders() {
		return thirdPartyDataProviderRepository.findAllThirdPartyDataProviders();
	}
}
