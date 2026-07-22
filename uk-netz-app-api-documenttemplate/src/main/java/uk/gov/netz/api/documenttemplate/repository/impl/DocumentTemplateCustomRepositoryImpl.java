package uk.gov.netz.api.documenttemplate.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchCriteria;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchResults;
import uk.gov.netz.api.documenttemplate.repository.DocumentTemplateCustomRepository;

@Repository
public class DocumentTemplateCustomRepositoryImpl implements DocumentTemplateCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DocumentTemplateSearchResults findBySearchCriteria(DocumentTemplateSearchCriteria searchCriteria) {
        return DocumentTemplateSearchResults.builder()
            .templates(constructResultsQuery(searchCriteria).getResultList())
            .total(((Number) constructCountQuery(searchCriteria).getSingleResult()).longValue())
            .build();
    }

    private Query constructResultsQuery(DocumentTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();

        sb.append(constructMainQueryStatement(searchCriteria))
            .append("order by name asc \n")
            .append("limit :limit \n")
            .append("offset :offset \n");

        return createQuery(sb.toString(), searchCriteria, false);
    }

    private Query constructCountQuery(DocumentTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();

        sb.append("select count(*) from ( \n")
            .append(constructMainQueryStatement(searchCriteria))
            .append(") results");

        return createQuery(sb.toString(), searchCriteria, true);
    }

    private String constructMainQueryStatement(DocumentTemplateSearchCriteria searchCriteria) {
        StringBuilder sb = new StringBuilder();

        sb.append("select id, name, role_type as roleType, workflow, last_updated_date as lastUpdatedDate \n")
            .append("from document_template \n")
            .append("where competent_authority = :competentAuthority \n");
        
        if (!ObjectUtils.isEmpty(searchCriteria.getRoleTypes())) {
            sb.append("and role_type = ANY(:roleTypes) \n");
        }
        
        if (StringUtils.hasText(searchCriteria.getTerm())) {
            sb.append("and (name ilike :term or workflow ilike :term) \n");
        }

        return sb.toString();
    }

    private Query createQuery(String sqlStatement, DocumentTemplateSearchCriteria searchCriteria, boolean forCount) {
        Query query = forCount
            ? entityManager.createNativeQuery(sqlStatement)
            : entityManager.createNativeQuery(sqlStatement, DocumentTemplate.DOCUMENT_TEMPLATE_INFO_DTO_RESULT_MAPPER);

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
