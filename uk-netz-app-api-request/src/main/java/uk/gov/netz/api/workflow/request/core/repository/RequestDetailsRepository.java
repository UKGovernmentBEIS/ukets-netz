package uk.gov.netz.api.workflow.request.core.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import uk.gov.netz.api.workflow.request.core.domain.QRequest;
import uk.gov.netz.api.workflow.request.core.domain.QRequestResource;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestStatuses;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsDTO;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsSearchResults;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestSearchCriteria;

import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RequestDetailsRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    public RequestDetailsSearchResults findRequestDetailsBySearchCriteria(RequestSearchCriteria criteria) {
        QRequest request = QRequest.request;
        QRequestResource requestResource = QRequestResource.requestResource;

        BooleanBuilder whereClause = new BooleanBuilder();
        
        whereClause.and(requestResource.resourceType.eq(criteria.getResourceType()));
        whereClause.and(requestResource.resourceId.eq(criteria.getResourceId()));
        whereClause.and(request.type.resourceType.eq(criteria.getResourceType()));
        whereClause.and(request.type.historyCategory.eq(criteria.getHistoryCategory()));
        
        if(!ObjectUtils.isEmpty(criteria.getRequestTypes())) {
        	whereClause.and(request.type.code.in(criteria.getRequestTypes()));
        }
        
        if (!criteria.getRequestStatuses().isEmpty()) {
            whereClause.and(request.status.in(criteria.getRequestStatuses()));
        }
        
        // handle not displayed in progress requests
        if (criteria.getRequestStatuses().isEmpty()
                || criteria.getRequestStatuses().contains(RequestStatuses.IN_PROGRESS)) {
            whereClause.andAnyOf(
                    criteria.getRequestStatuses().isEmpty() ? request.status.ne(RequestStatuses.IN_PROGRESS)
                            : request.status.in(criteria.getRequestStatuses().stream()
                                    .filter(status -> !status.equals(RequestStatuses.IN_PROGRESS)).collect(Collectors.toSet())),
                    request.type.displayedInProgress.eq(Boolean.TRUE));
        }

        JPAQuery<RequestDetailsDTO> query = new JPAQuery<>(entityManager);

        JPAQuery<RequestDetailsDTO> jpaQuery = query.select(
                Projections.constructor(RequestDetailsDTO.class,
                        request.id, 
                        request.type.code, 
                        request.status,
                        request.creationDate,
                        request.metadata))
                .from(request)
                .innerJoin(requestResource)
                .on(request.id.eq(requestResource.request.id))
                .where(whereClause)
                .orderBy(request.creationDate.desc())
                .offset((long)criteria.getPaging().getPageNumber() * criteria.getPaging().getPageSize())
                .limit(criteria.getPaging().getPageSize());

        return RequestDetailsSearchResults.builder()
                .requestDetails(jpaQuery.fetch())
                .total(jpaQuery.fetchCount())
                .build();
    }

    public Optional<RequestDetailsDTO> findRequestDetailsById(String requestId) {
        QRequest request = QRequest.request;

        JPAQuery<RequestDetailsDTO> query = new JPAQuery<>(entityManager);

        JPAQuery<RequestDetailsDTO> jpaQuery = query.select(
                Projections.constructor(RequestDetailsDTO.class,
                        request.id,
                        request.type.code,
                        request.status,
                        request.creationDate,
                        request.metadata))
                .from(request)
                .where(request.id.eq(requestId));

        return Optional.ofNullable(jpaQuery.fetchFirst());
    }
}
