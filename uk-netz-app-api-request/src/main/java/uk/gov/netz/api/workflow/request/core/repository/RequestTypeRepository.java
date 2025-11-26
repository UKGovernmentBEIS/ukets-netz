package uk.gov.netz.api.workflow.request.core.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.workflow.request.core.domain.RequestType;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {

	@Transactional(readOnly = true)
	Optional<RequestType> findByCode(String code);
	
	@Transactional(readOnly = true)
    Set<RequestType> findAllByCanCreateManually(boolean canCreateManually);
	
	@Transactional(readOnly = true)
    Set<RequestType> findAllByCanCreateManuallyAndResourceType(boolean canCreateManually, String resourceType);
}
