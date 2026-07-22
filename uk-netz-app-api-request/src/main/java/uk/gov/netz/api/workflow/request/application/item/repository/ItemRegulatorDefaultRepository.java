package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.account.domain.QAccount;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.core.domain.QRequest;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;

@Repository
public class ItemRegulatorDefaultRepository extends ItemRegulatorAbstractRepository {

    @Override
    protected void buildSearchTermJoin(JPAQuery<Item> jpaQuery) {
        QRequestResource requestResource2 = new QRequestResource("requestResource2");
        QRequest request = QRequest.request;
        QAccount account = QAccount.account;

        jpaQuery.
            innerJoin(requestResource2)
            .on(request.id.eq(requestResource2.request.id).and(requestResource2.resourceType.eq(ResourceType.ACCOUNT)))
            .innerJoin(account)
            .on(account.id.stringValue().eq(requestResource2.resourceId));
    }

    @Override
    protected BooleanExpression buildSearchTermWhereClause(String searchTerm) {
        QAccount account = QAccount.account;

        return account.name.containsIgnoreCase(searchTerm).or(account.businessId.eq(searchTerm));
    }
}