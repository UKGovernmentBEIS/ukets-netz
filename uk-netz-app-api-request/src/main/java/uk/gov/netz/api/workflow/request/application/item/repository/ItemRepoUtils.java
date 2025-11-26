package uk.gov.netz.api.workflow.request.application.item.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.experimental.UtilityClass;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.QRequest;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;
import uk.gov.netz.api.workflow.request.core.domain.QRequestTask;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class ItemRepoUtils {

    public BooleanExpression constructAccountRequestTaskScopeWhereClause(Map<Long, Set<String>> scopedAccountRequestTaskTypes,
                                                                         QRequestTask requestTask, QRequestResource requestResource) {
        List<BooleanExpression> orExpressions = new ArrayList<>();
        scopedAccountRequestTaskTypes.forEach((accountId, types) -> {
            orExpressions.add(requestResource.resourceType.eq(ResourceType.ACCOUNT)
            		.and(requestResource.resourceId.eq(accountId.toString()))
            		.and(requestTask.type.code.in(types)));
        });

        return Expressions.booleanTemplate(
                ItemRepoUtils.constructMultipleOrWhereTemplate(orExpressions.size()), orExpressions);
    }

    public BooleanExpression constructCARequestTaskScopeWhereClause(Map<CompetentAuthorityEnum, Set<String>> scopedCARequestTaskTypes,
                                                                    QRequestTask requestTask, QRequestResource requestResource) {
        List<BooleanExpression> orExpressions = new ArrayList<>();
        scopedCARequestTaskTypes.forEach((ca, types) -> {
            orExpressions.add(requestResource.resourceType.eq(ResourceType.CA)
            		.and(requestResource.resourceId.eq(ca.name()))
            		.and(requestTask.type.code.in(types)));
        });

        return Expressions.booleanTemplate(
                ItemRepoUtils.constructMultipleOrWhereTemplate(orExpressions.size()), orExpressions);
    }

    public BooleanExpression createSystemNotificationWhereClause(QRequest request, QRequestResource requestResource) {
        return request.type.code.eq(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .and(requestResource.resourceType.eq(ResourceType.ACCOUNT));
    }

    private String constructMultipleOrWhereTemplate(int scopedRequestTaskTypesSize) {
        StringBuilder templateBuilder;
        if (scopedRequestTaskTypesSize == 0) {
            templateBuilder = new StringBuilder("(1 = -1)");
        } else {
            templateBuilder = new StringBuilder("(({0})");
            for (int i = 1; i < scopedRequestTaskTypesSize; i++) {
                templateBuilder.append(" or ({").append(i).append("})");
            }
            templateBuilder.append(")");
        }
        return templateBuilder.toString();
    }
}