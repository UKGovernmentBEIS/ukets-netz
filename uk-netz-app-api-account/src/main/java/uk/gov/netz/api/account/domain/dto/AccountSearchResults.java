package uk.gov.netz.api.account.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class AccountSearchResults {

    private List<AccountSearchResultInfoDTO> accounts;
    private Long total;

    public static AccountSearchResults emptyAccountSearchResults() {
        return AccountSearchResults.builder().accounts(Collections.emptyList()).total(0L).build();
    }
}
