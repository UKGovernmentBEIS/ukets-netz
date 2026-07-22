package uk.gov.netz.api.documenttemplate.repository;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchCriteria;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchResults;

public interface DocumentTemplateCustomRepository {

    @Transactional(readOnly = true)
    DocumentTemplateSearchResults findBySearchCriteria(DocumentTemplateSearchCriteria searchCriteria);
}
