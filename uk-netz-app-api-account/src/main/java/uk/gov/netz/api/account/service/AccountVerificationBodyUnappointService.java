package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.event.AccountsVerificationBodyUnappointedEvent;
import uk.gov.netz.api.account.repository.AccountBaseRepository;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.account.service.validator.AccountStatus;
import uk.gov.netz.api.common.exception.BusinessException;

import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AccountVerificationBodyUnappointService {
    
    private final AccountRepository accountRepository;
    private final AccountBaseRepository<? extends Account> accountBaseRepository;
    private final AccountVbSiteContactService accountVbSiteContactService;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    @Transactional
    public void unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes(
            Long verificationBodyId, Set<String> notAvailableAccreditationEmissionTradingSchemes) {
        if (notAvailableAccreditationEmissionTradingSchemes.isEmpty()) {
            return;
        }
        
        Set<? extends Account> accountsToBeUnappointed = accountBaseRepository.findAllByVerificationBodyAndEmissionTradingScheme(
                verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);

        unappointAccounts(accountsToBeUnappointed);
    }

    @Transactional
    public void unappointAccountsAppointedToVerificationBody(Set<Long> verificationBodyIds) {
        Set<Account> accountsToBeUnappointed = accountRepository.findAllByVerificationWithContactsBodyIn(verificationBodyIds);
        unappointAccounts(accountsToBeUnappointed);
    }

    @AccountStatus(expression = "{#status != 'UNAPPROVED' && #status != 'DENIED'}")
    @Transactional
    public void unappointAccountAppointedToVerificationBody(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
        unappointAccounts(Set.of(account));
    }

    private void unappointAccounts(Set<? extends Account> accountsToBeUnappointed) {
        if (accountsToBeUnappointed.isEmpty()) {
            return;
        }
        
        //clear verification body of accounts
        accountsToBeUnappointed.forEach(account -> {
            account.setVerificationBodyId(null);
        });
        
        accountVbSiteContactService.removeVbSiteContactFromAccounts(accountsToBeUnappointed);

        // Notify users for unappointment
        accountVerificationBodyNotificationService.notifyUsersForVerificationBodyUnappointment(accountsToBeUnappointed);

        eventPublisher.publishEvent(AccountsVerificationBodyUnappointedEvent.builder()
            .accountIds(accountsToBeUnappointed.stream()
                .map(Account::getId)
                .collect(Collectors.toSet())
            )
            .build()
        );
    }
    
}
