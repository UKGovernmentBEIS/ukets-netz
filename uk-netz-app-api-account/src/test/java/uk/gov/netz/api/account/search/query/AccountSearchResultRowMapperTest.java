package uk.gov.netz.api.account.search.query;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSearchResultRowMapperTest {

    private final AccountSearchResultRowMapper mapper = new AccountSearchResultRowMapper();

    @Test
    void toDto_mapsCommonFields() {
        AccountSearchResultInfoDTO dto = mapper.toDto(
                new AccountSearchResultRow(1L, "Operator A", "BUS-1", "LIVE"));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Operator A");
        assertThat(dto.getBusinessId()).isEqualTo("BUS-1");
        assertThat(dto.getStatus()).isEqualTo("LIVE");
    }

    @Test
    void toDto_nullStatus() {
        AccountSearchResultInfoDTO dto = mapper.toDto(
                new AccountSearchResultRow(1L, "Operator A", "BUS-1", null));

        assertThat(dto.getStatus()).isNull();
    }

    @Test
    void toResults_buildsAccountSearchResults() {
        AccountSearchResults<AccountSearchResultInfoDTO> results = mapper.toResults(
                List.of(
                        new AccountSearchResultRow(1L, "A", "B-1", "LIVE"),
                        new AccountSearchResultRow(2L, "B", "B-2", "NEW")),
                42L);

        assertThat(results.getTotal()).isEqualTo(42L);
        assertThat(results.getAccounts()).hasSize(2);
        assertThat(results.getAccounts().get(0).getBusinessId()).isEqualTo("B-1");
        assertThat(results.getAccounts().get(1).getStatus()).isEqualTo("NEW");
    }

    @Test
    void emptyResults_returnsEmptyAccountSearchResults() {
        assertThat(mapper.emptyResults()).isEqualTo(AccountSearchResults.emptyAccountSearchResults());
    }
}
