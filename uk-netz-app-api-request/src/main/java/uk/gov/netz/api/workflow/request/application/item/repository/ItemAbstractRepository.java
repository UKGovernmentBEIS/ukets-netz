package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemSearchCriteriaDTO;
import uk.gov.netz.api.workflow.request.core.domain.QRequest;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;
import uk.gov.netz.api.workflow.request.core.domain.QRequestTask;

import java.util.Map;
import java.util.Set;

public abstract class ItemAbstractRepository {


    protected abstract void buildSearchTermJoin(JPAQuery<Item> jpaQuery);
    protected abstract BooleanExpression buildSearchTermWhereClause(String searchTerm);

    public Predicate constructWherePredicate(String userId, ItemAssignmentType assignmentType,
                                             QRequest request, QRequestTask requestTask, QRequestResource requestResource,
                                             Map<Long, Set<String>> scopedAccountRequestTaskTypes,
                                             ItemSearchCriteriaDTO searchCriteria) {
        String requestType = searchCriteria.getRequestType();
        String searchTerm = searchCriteria.getSearchTerm();

        BooleanExpression whereClause = ItemRepoUtils.constructAccountRequestTaskScopeWhereClause(
            scopedAccountRequestTaskTypes, requestTask, requestResource);

        whereClause = switch (assignmentType) {
            case ME -> whereClause.and(requestTask.assignee.eq(userId));
            case OTHERS -> whereClause.and(requestTask.assignee.ne(userId));
            case UNASSIGNED -> whereClause.and(requestTask.assignee.isNull());
        };

        if (!StringUtils.isBlank(requestType)) {
            whereClause = whereClause.and(request.type.code.eq(requestType));
        }

        if (!StringUtils.isBlank(searchTerm)) {
            whereClause = whereClause.and(buildSearchTermWhereClause(searchTerm));
        }

        if(ItemAssignmentType.ME == assignmentType && searchCriteria.hasNoFilters()) {
            whereClause = whereClause.or(ItemRepoUtils.createSystemNotificationWhereClause(request, requestTask, requestResource, userId));
        }

        return whereClause;
    }

}