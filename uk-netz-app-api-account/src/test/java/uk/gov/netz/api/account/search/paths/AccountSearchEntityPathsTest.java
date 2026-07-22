package uk.gov.netz.api.account.search.paths;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AccountSearchEntityPathsTest {

    @Test
    void testDouble_exposesConfiguredPaths() {
        @SuppressWarnings("unchecked")
        EntityPathBase<TestAccount> root = mock(EntityPathBase.class);
        NumberPath<Long> idPath = mock(NumberPath.class);
        StringPath namePath = mock(StringPath.class);
        StringPath businessIdPath = mock(StringPath.class);
        @SuppressWarnings("unchecked")
        EnumPath<CompetentAuthorityEnum> caPath = mock(EnumPath.class);
        StringPath statusPath = mock(StringPath.class);

        TestAccountSearchEntityPaths paths = new TestAccountSearchEntityPaths(
                root, idPath, namePath, businessIdPath, caPath, statusPath);

        assertThat(paths.root()).isSameAs(root);
        assertThat(paths.namePath()).isSameAs(namePath);
        assertThat(paths.businessIdPath()).isSameAs(businessIdPath);
        assertThat(paths.competentAuthorityPath()).isSameAs(caPath);
        assertThat(paths.statusPath()).isSameAs(statusPath);
        assertThat(paths.statusIn(null)).isNull();
    }
}
