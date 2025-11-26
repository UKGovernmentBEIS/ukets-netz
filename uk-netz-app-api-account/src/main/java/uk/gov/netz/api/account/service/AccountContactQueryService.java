package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.dto.AccountContactInfoDTO;
import uk.gov.netz.api.account.repository.AccountRepository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountContactQueryService {

    private final AccountRepository accountRepository;

    public Optional<String> findPrimaryContactByAccount(Long accountId) {
        return findContactByAccountAndContactType(accountId, AccountContactType.PRIMARY);
    }

    public Optional<String> findContactByAccountAndContactType(Long accountId, String accountContactType) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        return accountOpt
                .map(Account::getContacts)
                .map(contacts -> contacts.get(accountContactType));
    }

    public List<AccountContactInfoDTO> findContactsByAccountIdsAndContactType(Set<Long> accountIds, String accountContactType) {
        return accountRepository
                .findAccountContactsByAccountIdsAndContactType(new ArrayList<>(accountIds), accountContactType);
    }

    @Transactional(readOnly = true)
    public Map<String, String> findContactTypesByAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent() && !accountOpt.get().getContacts().isEmpty()) {
            return new HashMap<>(accountOpt.get().getContacts());
        }
        return Map.of();
    }

    @Transactional(readOnly = true)
    public Map<String, String> findOperatorContactTypesByAccount(Long accountId) {
        Map<String, String> contactTypesByAccount = findContactTypesByAccount(accountId);
        return contactTypesByAccount.entrySet().stream()
                .filter(accountContactType -> AccountContactType.getOperatorAccountContactTypes().contains(accountContactType.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
