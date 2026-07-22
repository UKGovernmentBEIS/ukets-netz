package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import uk.gov.netz.api.account.domain.QAccount;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchCommonSortField;
import uk.gov.netz.api.account.search.criteria.AccountSearchSortField;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountSearchSortMapperTest {

    private final AccountSearchSortMapper<uk.gov.netz.api.account.domain.Account> mapper =
            new AccountSearchSortMapper<>();

    private QAccountTestEntityPaths paths;

    @BeforeEach
    void setUp() {
        paths = new QAccountTestEntityPaths();
    }

    @Test
    void operatorNameAsc() {
        OrderSpecifier<?> order = mapper.toOrderSpecifier(
                AccountSearchCommonSortField.OPERATOR_NAME, Sort.Direction.ASC, paths);

        assertThat(order.toString()).isEqualTo("account.name ASC");
    }

    @Test
    void operatorNameDesc() {
        OrderSpecifier<?> order = mapper.toOrderSpecifier(
                AccountSearchCommonSortField.OPERATOR_NAME, Sort.Direction.DESC, paths);

        assertThat(order.toString()).isEqualTo("account.name DESC");
    }

    @Test
    void accountIdAsc() {
        OrderSpecifier<?> order = mapper.toOrderSpecifier(
                AccountSearchCommonSortField.ACCOUNT_ID, Sort.Direction.ASC, paths);

        assertThat(order.toString()).isEqualTo("account.businessId ASC");
    }

    @Test
    void statusAsc() {
        paths.setStatusPath(QAccount.account.businessId);

        OrderSpecifier<?> order = mapper.toOrderSpecifier(
                AccountSearchCommonSortField.STATUS, Sort.Direction.ASC, paths);

        assertThat(order.toString()).isEqualTo("account.businessId ASC");
    }

    @Test
    void statusSortFailsWhenPathNotSupported() {
        assertThatThrownBy(() -> mapper.toOrderSpecifier(
                AccountSearchCommonSortField.STATUS, Sort.Direction.ASC, paths))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sort field not supported: STATUS");
    }

    @Test
    void fromCriteria_usesEnumSortFieldNotRawStrings() {
        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .paging(PagingRequest.builder().pageNumber(0).pageSize(20).build())
                .sortField(AccountSearchCommonSortField.OPERATOR_NAME)
                .sortDirection(Sort.Direction.ASC)
                .build();

        OrderSpecifier<?> order = mapper.fromCriteria(criteria, paths);

        assertThat(order.toString()).isEqualTo("account.name ASC");
        assertThat(mapperAcceptsOnlyEnumSortField()).isTrue();
    }

    @Test
    void unsupportedSortField_throwsIllegalArgumentException() {
        AccountSearchSortField unsupportedSortField = new AccountSearchSortField() { };

        assertThatThrownBy(() -> mapper.toOrderSpecifier(
                unsupportedSortField, Sort.Direction.ASC, paths))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sort field not supported:");
    }

    private static boolean mapperAcceptsOnlyEnumSortField() {
        return Arrays.stream(AccountSearchSortMapper.class.getDeclaredMethods())
                .filter(method -> "toOrderSpecifier".equals(method.getName()))
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .noneMatch(String.class::equals);
    }
}
