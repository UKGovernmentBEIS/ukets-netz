package uk.gov.netz.api.account.search.criteria;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSearchFilterCriteriaTest {

    private static PagingRequest paging() {
        return PagingRequest.builder().pageNumber(0).pageSize(20).build();
    }

    @Test
    void builder_defaults_sortToOperatorNameAsc() {
        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .paging(paging())
                .build();

        assertThat(criteria.getSortField()).isEqualTo(AccountSearchCommonSortField.OPERATOR_NAME);
        assertThat(criteria.getSortDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void hasTerm_trueWhenPresent() {
        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .term("ship")
                .paging(paging())
                .build();

        assertThat(criteria.hasTerm()).isTrue();
    }

    @Test
    void hasTerm_falseWhenBlank() {
        assertThat(AccountSearchFilterCriteria.builder().term("   ").paging(paging()).build().hasTerm()).isFalse();
        assertThat(AccountSearchFilterCriteria.builder().term(null).paging(paging()).build().hasTerm()).isFalse();
    }

    @Test
    void hasStatusFilter_trueWhenStatusesPresent() {
        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .statuses(Set.of(TestAccountStatus.DUMMY))
                .paging(paging())
                .build();

        assertThat(criteria.hasStatusFilter()).isTrue();
    }

    @Test
    void hasStatusFilter_falseWhenNullOrEmpty() {
        assertThat(AccountSearchFilterCriteria.builder().paging(paging()).build().hasStatusFilter()).isFalse();
        assertThat(AccountSearchFilterCriteria.builder().statuses(Set.of()).paging(paging()).build().hasStatusFilter()).isFalse();
    }

    @Test
    void hasContactEmail_trueWhenPresent() {
        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .contactEmail("user@example.com")
                .paging(paging())
                .build();

        assertThat(criteria.hasContactEmail()).isTrue();
    }

    @Test
    void hasContactEmail_falseWhenBlank() {
        assertThat(AccountSearchFilterCriteria.builder().contactEmail("  ").paging(paging()).build().hasContactEmail()).isFalse();
        assertThat(AccountSearchFilterCriteria.builder().contactEmail(null).paging(paging()).build().hasContactEmail()).isFalse();
    }
}
