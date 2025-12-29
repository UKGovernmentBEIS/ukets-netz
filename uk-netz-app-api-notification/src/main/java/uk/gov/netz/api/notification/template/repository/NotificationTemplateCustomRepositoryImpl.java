package uk.gov.netz.api.notification.template.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchCriteria;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchResults;

@Repository
public class NotificationTemplateCustomRepositoryImpl implements NotificationTemplateCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public NotificationTemplateSearchResults findBySearchCriteria(NotificationTemplateSearchCriteria searchCriteria) {
        return NotificationTemplateSearchResults.builder()
            .templates(constructResultsQuery(searchCriteria).getResultList())
            .total(((Number) constructCountQuery(searchCriteria).getSingleResult()).longValue())
            .build();
    }

    private Query constructResultsQuery(NotificationTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();

        sb.append(constructMainQueryStatement(searchCriteria))
            .append("order by name asc \n")
            .append("limit :limit \n")
            .append("offset :offset \n");

        return createQuery(sb.toString(), searchCriteria, false);
    }

    private Query constructCountQuery(NotificationTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();

        sb.append("select count(*) from ( \n")
            .append(constructMainQueryStatement(searchCriteria))
            .append(") results");

        return createQuery(sb.toString(), searchCriteria, true);
    }
    
    private String constructMainQueryStatement(NotificationTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select id, name, role_type as roleType, workflow, last_updated_date as lastUpdatedDate \n")
                .append("from notification_template \n")
                .append("where is_managed = :managed \n")
                .append("and competent_authority = :competentAuthority \n");
        
        if (!ObjectUtils.isEmpty(searchCriteria.getRoleTypes())) {
            sb.append("and role_type = ANY(:roleTypes) \n");
        }
        
        if (StringUtils.hasText(searchCriteria.getTerm())) {
            sb.append("and (name ilike :term or workflow ilike :term) \n");
        }
        
        return sb.toString();
    }

    private Query createQuery(String sqlStatement, NotificationTemplateSearchCriteria searchCriteria, boolean forCount) {
        Query query = forCount
            ? entityManager.createNativeQuery(sqlStatement)
            : entityManager.createNativeQuery(sqlStatement, NotificationTemplate.NOTIFICATION_TEMPLATE_INFO_DTO_RESULT_MAPPER);

        query.setParameter("managed", true);
        query.setParameter("competentAuthority", searchCriteria.getCompetentAuthority().name());

        if (StringUtils.hasText(searchCriteria.getTerm())) {
            query.setParameter("term", "%" + searchCriteria.getTerm() + "%");
        }
        
        if (!ObjectUtils.isEmpty(searchCriteria.getRoleTypes())) {
            query.setParameter("roleTypes", searchCriteria.getRoleTypes().toArray(new String[0]));
        }

        if (!forCount) {
            query.setParameter("limit", searchCriteria.getPaging().getPageSize());
            query.setParameter("offset", (long)searchCriteria.getPaging().getPageNumber() * searchCriteria.getPaging().getPageSize());
        }

        return query;
    }
}
