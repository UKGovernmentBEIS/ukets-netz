package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderQueryService;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountThirdPartyDataProviderService {

    private final AccountQueryService accountQueryService;
    private final ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;
    private final ThirdPartyDataProviderService thirdPartyDataProviderService;

    public Optional<ThirdPartyDataProviderNameInfoDTO> getThirdPartyDataProviderNameInfoByAccount(Long accountId) {
        return accountQueryService
            .getThirdPartyDataProviderId(accountId)
            .map(thirdPartyDataProviderService::getThirdPartyDataProviderNameInfoById);
    }

    public List<ThirdPartyDataProviderNameInfoDTO> getAllThirdPartyDataProviders() {
        return thirdPartyDataProviderQueryService.getAllThirdPartyDataProviders();
    }
}
