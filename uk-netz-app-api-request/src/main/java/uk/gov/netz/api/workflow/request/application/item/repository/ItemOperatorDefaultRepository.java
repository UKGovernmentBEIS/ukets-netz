package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.account.domain.QAccount;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;

@Repository
public class ItemOperatorDefaultRepository extends ItemOperatorAbstractRepository {

    @Override
    protected void buildSearchTermJoin(JPAQuery<Item> jpaQuery) {
        QRequestResource requestResource = QRequestResource.requestResource;
        QAccount account = QAccount.account;

        jpaQuery
            .innerJoin(account)
            .on(account.id.stringValue().eq(requestResource.resourceId)
            .and(requestResource.resourceType.eq(ResourceType.ACCOUNT)));
    }

    @Override
    protected BooleanExpression buildSearchTermWhereClause(String searchTerm) {
        QAccount account = QAccount.account;

        return account.name.containsIgnoreCase(searchTerm).or(account.businessId.eq(searchTerm));
    }
}