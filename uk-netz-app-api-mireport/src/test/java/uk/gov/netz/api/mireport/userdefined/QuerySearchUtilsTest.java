package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class QuerySearchUtilsTest {

    @Test
    void returnsNullForNull() {
        assertThat(QuerySearchUtils.toSearchPattern(null)).isNull();
    }

    @Test
    void returnsNullForBlank() {
        assertThat(QuerySearchUtils.toSearchPattern("   ")).isNull();
    }

    @Test
    void lowercasesAndWrapsWithWildcards() {
        assertThat(QuerySearchUtils.toSearchPattern("Report")).isEqualTo("%report%");
    }

    @Test
    void escapesPercentWildcard() {
        assertThat(QuerySearchUtils.toSearchPattern("50%")).isEqualTo("%50\\%%");
    }

    @Test
    void escapesUnderscoreWildcard() {
        assertThat(QuerySearchUtils.toSearchPattern("a_b")).isEqualTo("%a\\_b%");
    }

    @Test
    void escapesBackslashFirst() {
        assertThat(QuerySearchUtils.toSearchPattern("a\\b")).isEqualTo("%a\\\\b%");
    }

    @Test
    void escapesCombinationOfSpecialCharacters() {
        assertThat(QuerySearchUtils.toSearchPattern("A_50%\\x"))
                .isEqualTo("%a\\_50\\%\\\\x%");
    }
}