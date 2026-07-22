package uk.gov.netz.api.account.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class AccountSearchResults<SR extends AccountSearchResultInfoDTO> {

    private List<SR> accounts;
    private Long total;

    public static <SR extends AccountSearchResultInfoDTO> AccountSearchResults<SR> empty() {
        return AccountSearchResults.<SR>builder()
                .accounts(Collections.emptyList())
                .total(0L)
                .build();
    }

    public static AccountSearchResults<AccountSearchResultInfoDTO> emptyAccountSearchResults() {
        return empty();
    }
}
