package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.account.domain.QAccount;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;

/** Test paths backed by {@link QAccount}. */
class QAccountTestEntityPaths implements AccountSearchEntityPaths<Account> {

    private static final QAccount ACCOUNT = QAccount.account;

    private BooleanExpression statusInResult;
    private StringPath statusPath;

    void setStatusPath(StringPath statusPath) {
        this.statusPath = statusPath;
    }

    void setStatusInResult(BooleanExpression statusInResult) {
        this.statusInResult = statusInResult;
    }

    @Override
    public EntityPathBase<Account> root() {
        return ACCOUNT;
    }

    @Override
    public NumberPath<Long> idPath() {
        return ACCOUNT.id;
    }

    @Override
    public StringPath namePath() {
        return ACCOUNT.name;
    }

    @Override
    public StringPath businessIdPath() {
        return ACCOUNT.businessId;
    }

    @Override
    public EnumPath<CompetentAuthorityEnum> competentAuthorityPath() {
        return ACCOUNT.competentAuthority;
    }

    @Override
    public StringExpression statusPath() {
        return statusPath;
    }

    @Override
    public BooleanExpression statusIn(Set<? extends AccountStatus> statuses) {
        return statusInResult;
    }
}
