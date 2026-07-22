package uk.gov.netz.api.account;

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

/**
 * Manual QueryDSL path for {@link TestAccount} (test entity is not processed by apt).
 */
public class QTestAccount extends EntityPathBase<TestAccount> {

    private static final long serialVersionUID = 1L;

    public static final QTestAccount testAccount = new QTestAccount("testAccount");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final StringPath name = createString("name");
    public final StringPath businessId = createString("businessId");
    public final EnumPath<CompetentAuthorityEnum> competentAuthority =
            createEnum("competentAuthority", CompetentAuthorityEnum.class);
    public final EnumPath<TestAccountStatus> status = createEnum("status", TestAccountStatus.class);

    public QTestAccount(String variable) {
        super(TestAccount.class, PathMetadataFactory.forVariable(variable));
    }
}
