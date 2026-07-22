package uk.gov.netz.api.documenttemplate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;

import java.util.Optional;
import java.util.Set;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long>, DocumentTemplateCustomRepository {

	Optional<DocumentTemplate> findByTypeAndCompetentAuthority(String type,
			CompetentAuthorityEnum competentAuthority);
	
	Set<DocumentTemplate> findAllByNotificationTemplateId(Long notificationTemplateId);
}
