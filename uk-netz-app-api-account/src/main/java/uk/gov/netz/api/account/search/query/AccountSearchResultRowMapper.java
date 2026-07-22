package uk.gov.netz.api.account.search.query;

import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;

import java.util.List;

/** Maps projected rows to shared account search API DTOs. */
public class AccountSearchResultRowMapper
        implements AccountSearchQueryResultsMapper<AccountSearchResultRow, AccountSearchResultInfoDTO> {

    public AccountSearchResultInfoDTO toDto(AccountSearchResultRow row) {
        return new AccountSearchResultInfoDTO(
                row.getId(),
                row.getName(),
                row.getBusinessId(),
                toAccountStatus(row.getStatus()));
    }

    @Override
    public AccountSearchResults<AccountSearchResultInfoDTO> toResults(
            List<AccountSearchResultRow> rows, long total) {
        return AccountSearchResults.<AccountSearchResultInfoDTO>builder()
                .accounts(rows.stream().map(this::toDto).toList())
                .total(total)
                .build();
    }

    @Override
    public AccountSearchResults<AccountSearchResultInfoDTO> emptyResults() {
        return AccountSearchResults.emptyAccountSearchResults();
    }

    private static AccountStatus toAccountStatus(String statusName) {
        return statusName == null ? null : () -> statusName;
    }
}
