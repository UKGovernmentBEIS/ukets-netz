package uk.gov.netz.api.account.search.paths;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;

/** Scheme-specific QueryDSL paths for generic account search. */
public interface AccountSearchEntityPaths<T extends Account> {

    EntityPathBase<T> root();

    NumberPath<Long> idPath();

    StringPath namePath();

    StringPath businessIdPath();

    EnumPath<CompetentAuthorityEnum> competentAuthorityPath();

    /** Null if status is not on the subtype entity. Use {@code enumPath.stringValue()} for enum columns. */
    StringExpression statusPath();

    /** Null when statuses is empty. */
    BooleanExpression statusIn(Set<? extends AccountStatus> statuses);
}
