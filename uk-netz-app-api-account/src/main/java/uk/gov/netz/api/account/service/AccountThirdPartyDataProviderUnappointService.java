package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.account.service.validator.AccountStatus;
import uk.gov.netz.api.common.exception.BusinessException;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AccountThirdPartyDataProviderUnappointService {
    
    private final AccountRepository accountRepository;

    @AccountStatus(expression = "{#status != 'UNAPPROVED' && #status != 'DENIED'}")
    @Transactional
    public void unappointAccountAppointedToThirdPartyDataProvider(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
        account.setThirdPartyDataProviderId(null);
    }
}
