package uk.gov.netz.api.account.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.account.service.validator.AccountContactTypeDeleteValidator;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AccountContactDeletionService {

    private final AccountRepository accountRepository;
    private final List<AccountContactTypeDeleteValidator> accountContactTypeDeleteValidators;

    @Transactional
    public void removeUserFromAccountContacts(String userId, Long accountId) {
        Account account = getAccount(accountId);
        Map<String, String> accountContacts = account.getContacts();
        if (accountContacts.containsValue(userId)) {
            //first remove user from account contacts and then validate account contacts
            accountContacts.entrySet().removeIf(entry -> userId.equals(entry.getValue()));
            accountContactTypeDeleteValidators.forEach(validator -> validator.validateDelete(accountContacts));
        }
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
