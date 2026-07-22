package uk.gov.netz.api.account.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Query result row for generic account search projection. */
@Getter
@AllArgsConstructor
public class AccountSearchResultRow {

    private final Long id;
    private final String name;
    private final String businessId;
    private final String status;
}
