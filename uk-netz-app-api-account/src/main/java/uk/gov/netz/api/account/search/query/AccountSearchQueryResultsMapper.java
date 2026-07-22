package uk.gov.netz.api.account.search.query;

import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;

import java.util.List;

/** Maps projected query rows to {@link AccountSearchResults}. */
public interface AccountSearchQueryResultsMapper<
        R extends AccountSearchResultRow,
        SR extends AccountSearchResultInfoDTO> {

    AccountSearchResults<SR> toResults(List<R> rows, long total);

    AccountSearchResults<SR> emptyResults();
}
