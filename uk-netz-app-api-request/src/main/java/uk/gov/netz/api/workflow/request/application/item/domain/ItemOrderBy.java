package uk.gov.netz.api.workflow.request.application.item.domain;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import uk.gov.netz.api.workflow.request.core.domain.QRequestTask;

@Getter
public enum ItemOrderBy {
    NEWEST_FIRST(QRequestTask.requestTask.startDate.desc()),
    OLDEST_FIRST(QRequestTask.requestTask.startDate.asc()),
    NEAREST_DUE_DATE(Expressions.numberTemplate(
        Integer.class,
        "{0} - COALESCE({1}, CURRENT_DATE)",
        QRequestTask.requestTask.dueDate,
        QRequestTask.requestTask.pauseDate
    ).asc().nullsLast()
    );

    private final OrderSpecifier<?> orderSpecifier;

    ItemOrderBy(OrderSpecifier<?> orderSpecifier) {
        this.orderSpecifier = orderSpecifier;
    }

}
