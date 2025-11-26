package uk.gov.netz.api.account.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.repository.AccountSearchAdditionalKeywordRepository;

@Service
@RequiredArgsConstructor
public class AccountSearchAdditionalKeywordService {

    private final AccountSearchAdditionalKeywordRepository accountSearchAdditionalKeywordRepository;

    @Transactional
    public void storeKeywordsForAccount(Long accountId, Map<String, @NotNull String> searchKeywordPairs) {
        final Map<String, AccountSearchAdditionalKeyword> existingKeywordByKey =
        		accountSearchAdditionalKeywordRepository.findByAccountId(accountId).stream()
                        .collect(HashMap::new, (map, keyword) -> map.put(keyword.getKey(), keyword), HashMap::putAll);

        final List<AccountSearchAdditionalKeyword> accountSearchAdditionalKeywords =
                searchKeywordPairs.entrySet().stream()
                        .map(entry -> Optional.ofNullable(existingKeywordByKey.get(entry.getKey()))
                                .map(accountSearchAdditionalKeyword -> {
                                    accountSearchAdditionalKeyword.setValue(entry.getValue());
                                    return accountSearchAdditionalKeyword;
                                })
                                .orElseGet(() -> (AccountSearchAdditionalKeyword.builder()
                                        .accountId(accountId)
                                        .value(entry.getValue())
                                        .key(entry.getKey())
                                        .build())))
                        .collect(Collectors.toList());

        accountSearchAdditionalKeywordRepository.saveAll(accountSearchAdditionalKeywords);
    }
    
	@Transactional
	public void deleteAllByAccountId(Long accountId) {
		accountSearchAdditionalKeywordRepository.deleteAllByAccountId(accountId);
	}
}
