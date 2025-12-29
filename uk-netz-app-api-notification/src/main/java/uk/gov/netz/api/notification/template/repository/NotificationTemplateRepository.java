package uk.gov.netz.api.notification.template.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;

import java.util.Optional;

/**
 * Repository for {@link NotificationTemplate} objects.
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long>, NotificationTemplateCustomRepository {

	Optional<NotificationTemplate> findByNameAndCompetentAuthority(String name, CompetentAuthorityEnum competentAuthority);

    @EntityGraph(value = "notification-template-with-text", type = EntityGraph.EntityGraphType.FETCH)
    @Query(name = NotificationTemplate.NAMED_QUERY_FIND_MANAGED_NOTIFICATION_TEMPLATE_BY_ID)
    Optional<NotificationTemplate> findManagedNotificationTemplateByIdWithText(Long id);

    @Query(name = NotificationTemplate.NAMED_QUERY_FIND_MANAGED_NOTIFICATION_TEMPLATE_BY_ID)
    Optional<NotificationTemplate> findManagedNotificationTemplateById(Long id);
}
