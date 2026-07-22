package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
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

public abstract class ItemOperatorAbstractRepository extends ItemAbstractRepository {

    @PersistenceContext
    private EntityManager entityManager;

    protected abstract void buildSearchTermJoin(JPAQuery<Item> jpaQuery);
    protected abstract BooleanExpression buildSearchTermWhereClause(String searchTerm);

    public ItemPage findItems(String userId,
                              ItemAssignmentType assignmentType,
                              Map<Long, Set<String>> scopedAccountRequestTaskTypes,
                              PagingRequest paging,
                              ItemSearchCriteriaDTO searchCriteria) {
        QRequest request = QRequest.request;
        QRequestTask requestTask = QRequestTask.requestTask;
        QRequestTaskVisit requestTaskVisit = QRequestTaskVisit.requestTaskVisit;
        QRequestResource requestResource = QRequestResource.requestResource;

        JPAQuery<Item> query = new JPAQuery<>(entityManager);

        JPAQuery<Item> jpaQuery = query.select(
                        Projections.constructor(Item.class,
                                requestTask.startDate,
                                request.id, request.type,
                                requestTask.id, requestTask.type, requestTask.assignee,
                                requestTask.dueDate, requestTask.pauseDate, requestTaskVisit.isNull()))
                .from(request)
                .innerJoin(requestResource)
                .on(request.id.eq(requestResource.request.id))
                .innerJoin(requestTask)
                .on(request.id.eq(requestTask.request.id))
                .leftJoin(requestTaskVisit)
                .on(requestTask.id.eq(requestTaskVisit.taskId).and(requestTaskVisit.userId.eq(userId)))
                .where(constructWherePredicate(userId, assignmentType, request, requestTask, requestResource,
                    scopedAccountRequestTaskTypes, searchCriteria));

        if (!StringUtils.isBlank(searchCriteria.getSearchTerm())) {
            buildSearchTermJoin(jpaQuery);
        }

        jpaQuery.orderBy(searchCriteria.getOrderBy().getOrderSpecifier())
                .offset((long)paging.getPageNumber() * paging.getPageSize())
                .limit(paging.getPageSize());


        return ItemPage.builder()
                .items(jpaQuery.fetch())
                .totalItems(jpaQuery.fetchCount())
                .build();
    }
}