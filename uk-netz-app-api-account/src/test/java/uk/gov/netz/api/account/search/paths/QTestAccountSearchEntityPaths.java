package uk.gov.netz.api.account.search.paths;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import uk.gov.netz.api.account.QTestAccount;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;
import java.util.stream.Collectors;

/** {@link AccountSearchEntityPaths} for {@link TestAccount} integration tests. */
public final class QTestAccountSearchEntityPaths implements AccountSearchEntityPaths<TestAccount> {

    private static final QTestAccount ACCOUNT = QTestAccount.testAccount;

    @Override
    public EntityPathBase<TestAccount> root() {
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
        return ACCOUNT.status.stringValue();
    }

    @Override
    public BooleanExpression statusIn(Set<? extends AccountStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        Set<TestAccountStatus> mappedStatuses = statuses.stream()
                .map(status -> TestAccountStatus.valueOf(status.getName()))
                .collect(Collectors.toSet());
        return ACCOUNT.status.in(mappedStatuses);
    }
}
