package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.repository.AccountSearchAdditionalKeywordRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccountSearchAdditionalKeywordServiceTest {
    private static final long ACCOUNT_ID = 1L;

    @InjectMocks
    private AccountSearchAdditionalKeywordService service;

    @Mock
    private AccountSearchAdditionalKeywordRepository accountSearchAdditionalKeywordRepository;

    @Test
    void storeKeywordsForAccount() {
        String keyword1 = "test1";
        String keyword2 = "test2";
        String key1 = "key1test1";
        String key2 = "key2test1";

        final Map<String, String> keyValuePairs = new TreeMap<>(Map.of(key1, keyword1, key2, keyword2));

        service.storeKeywordsForAccount(ACCOUNT_ID, keyValuePairs);

        verify(accountSearchAdditionalKeywordRepository).saveAll(
                List.of(
                        createAccountSearchAdditionalKeyword(key1, keyword1),
                        createAccountSearchAdditionalKeyword(key2, keyword2)
                )
        );

        verify(accountSearchAdditionalKeywordRepository, times(1)).findByAccountId(ACCOUNT_ID);
        verify(accountSearchAdditionalKeywordRepository, times(1)).saveAll(any());
    }

    private AccountSearchAdditionalKeyword createAccountSearchAdditionalKeyword(String key, String term) {
        return AccountSearchAdditionalKeyword.builder()
                .accountId(ACCOUNT_ID)
                .value(term)
                .key(key)
                .build();
    }
}
