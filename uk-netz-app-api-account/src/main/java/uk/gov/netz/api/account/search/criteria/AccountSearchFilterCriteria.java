package uk.gov.netz.api.account.search.criteria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.Set;

/** Request filters and paging for generic account search. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSearchFilterCriteria {

    private String term;

    private Set<? extends AccountStatus> statuses;

    /** Resolved to account IDs before querying. */
    private String contactEmail;

    @Valid
    @NotNull
    private PagingRequest paging;

    @Builder.Default
    private AccountSearchSortField sortField = AccountSearchCommonSortField.OPERATOR_NAME;

    @Builder.Default
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    public boolean hasTerm() {
        return StringUtils.hasText(term);
    }

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasContactEmail() {
        return StringUtils.hasText(contactEmail);
    }
}
