package uk.gov.netz.api.workflow.request.core.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;

@Repository
public interface RequestTaskActionTypeRepository extends JpaRepository<RequestTaskActionType, Long> {

	@Transactional(readOnly = true)
    Set<RequestTaskActionType> findAllByBlockedByPayment(boolean blockedByPayment);
	
}
