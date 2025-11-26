package uk.gov.netz.api.workflow.request.core.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

@Repository
public interface RequestTaskTypeRepository extends JpaRepository<RequestTaskType, Long> {

	Optional<RequestTaskType> findByCode(String code);

	@Transactional(readOnly = true)
	Set<RequestTaskType> findAllByCodeEndingWith(String code);
	
	@Transactional(readOnly = true)
	Set<RequestTaskType> findAllByCodeEndingWithOrCodeEndingWith(String code1, String code2);
	
}
