package uk.gov.netz.api.account.search.paths;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;

/** Test double for {@link AccountSearchEntityPaths}; used by future repository tests. */
public class TestAccountSearchEntityPaths implements AccountSearchEntityPaths<TestAccount> {

    private final EntityPathBase<TestAccount> root;
    private final NumberPath<Long> idPath;
    private final StringPath namePath;
    private final StringPath businessIdPath;
    private final EnumPath<CompetentAuthorityEnum> competentAuthorityPath;
    private final StringPath statusPath;

    public TestAccountSearchEntityPaths(
            EntityPathBase<TestAccount> root,
            NumberPath<Long> idPath,
            StringPath namePath,
            StringPath businessIdPath,
            EnumPath<CompetentAuthorityEnum> competentAuthorityPath,
            StringPath statusPath) {
        this.root = root;
        this.idPath = idPath;
        this.namePath = namePath;
        this.businessIdPath = businessIdPath;
        this.competentAuthorityPath = competentAuthorityPath;
        this.statusPath = statusPath;
    }

    @Override
    public EntityPathBase<TestAccount> root() {
        return root;
    }

    @Override
    public NumberPath<Long> idPath() {
        return idPath;
    }

    @Override
    public StringPath namePath() {
        return namePath;
    }

    @Override
    public StringPath businessIdPath() {
        return businessIdPath;
    }

    @Override
    public EnumPath<CompetentAuthorityEnum> competentAuthorityPath() {
        return competentAuthorityPath;
    }

    @Override
    public StringExpression statusPath() {
        return statusPath;
    }

    @Override
    public BooleanExpression statusIn(Set<? extends AccountStatus> statuses) {
        return null;
    }
}
