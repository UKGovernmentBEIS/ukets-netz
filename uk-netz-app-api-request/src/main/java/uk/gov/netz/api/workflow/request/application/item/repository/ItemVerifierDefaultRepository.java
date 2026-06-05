package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.account.domain.QAccount;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.QRequestTaskVisit;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemSearchCriteriaDTO;
import uk.gov.netz.api.workflow.request.core.domain.QRequest;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;
import uk.gov.netz.api.workflow.request.core.domain.QRequestTask;

import java.util.Map;
import java.util.Set;

@Repository
public class ItemVerifierDefaultRepository extends ItemVerifierAbstractRepository {

    @Override
    protected void buildSearchTermJoin(JPAQuery<Item> jpaQuery) {
        QRequestResource requestResource =  QRequestResource.requestResource;
        QAccount account = QAccount.account;

        jpaQuery
            .innerJoin(account)
            .on(account.id.stringValue().eq(requestResource.resourceId)
                .and(requestResource.resourceType.eq(ResourceType.ACCOUNT)));
    }

    @Override
    protected BooleanExpression buildSearchTermWhereClause(String searchTerm) {
        QAccount account = QAccount.account;

        return account.name.containsIgnoreCase(searchTerm)
            .or(account.businessId.eq(searchTerm));
    }

}