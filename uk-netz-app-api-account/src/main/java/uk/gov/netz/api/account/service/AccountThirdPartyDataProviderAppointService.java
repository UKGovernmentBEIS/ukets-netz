package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderQueryService;

@RequiredArgsConstructor
@Service
public class AccountThirdPartyDataProviderAppointService {
    
    private final AccountQueryService accountQueryService;
    private final ApprovedAccountQueryService approvedAccountQueryService;
    private final ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;

    @Transactional
    public void appointThirdPartyDataProviderToAccount(Long thirdPartyDataProviderId, Long accountId) {
        // Validate that account exist and is in approved state
        Account account = accountQueryService.getAccountById(accountId);

        if (!approvedAccountQueryService.isAccountApproved(account)) {
            throw new BusinessException(ErrorCode.ACCOUNT_INVALID_STATUS);
        }
        
        if (!thirdPartyDataProviderQueryService.existsById(thirdPartyDataProviderId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, thirdPartyDataProviderId);
        }

        if (thirdPartyDataProviderId.equals(account.getThirdPartyDataProviderId())) {
            throw new BusinessException(ErrorCode.THIRD_PARTY_DATA_PROVIDER_ALREADY_APPOINTED_TO_ACCOUNT);
        }
        
        //appoint
        account.setThirdPartyDataProviderId(thirdPartyDataProviderId);
    }
}
