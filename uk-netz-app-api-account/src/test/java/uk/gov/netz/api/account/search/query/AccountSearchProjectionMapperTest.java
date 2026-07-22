package uk.gov.netz.api.account.search.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.account.domain.QAccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountSearchProjectionMapperTest {

    private AccountSearchResultRowProjectionMapper<uk.gov.netz.api.account.domain.Account> mapper;
    private QAccountTestEntityPaths paths;

    @BeforeEach
    void setUp() {
        mapper = new AccountSearchResultRowProjectionMapper<>();
        paths = new QAccountTestEntityPaths();
    }

    @Test
    void constructorProjection_useCommonPaths() {
        paths.setStatusPath(QAccount.account.businessId);

        var expressions = mapper.constructorProjection(paths).getArgs();

        assertThat(expressions).hasSize(4);
        assertThat(expressions.get(0).toString()).isEqualTo("account.id");
        assertThat(expressions.get(1).toString()).isEqualTo("account.name");
        assertThat(expressions.get(2).toString()).isEqualTo("account.businessId");
        assertThat(expressions.get(3).toString()).isEqualTo("account.businessId");
    }

    @Test
    void constructorProjection_failWhenStatusPathMissing() {
        assertThatThrownBy(() -> mapper.constructorProjection(paths))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status path is required for account search projection");
    }

    @Test
    void constructorProjection_targetsAccountSearchResultRow() {
        paths.setStatusPath(QAccount.account.businessId);

        assertThat(mapper.constructorProjection(paths).getType())
                .isEqualTo(AccountSearchResultRow.class);
    }
}
