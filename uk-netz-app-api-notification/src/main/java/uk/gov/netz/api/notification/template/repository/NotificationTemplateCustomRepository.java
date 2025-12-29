package uk.gov.netz.api.notification.template.repository;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchCriteria;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateSearchResults;

public interface NotificationTemplateCustomRepository {

    @Transactional(readOnly = true)
    NotificationTemplateSearchResults findBySearchCriteria(NotificationTemplateSearchCriteria searchCriteria);
}
