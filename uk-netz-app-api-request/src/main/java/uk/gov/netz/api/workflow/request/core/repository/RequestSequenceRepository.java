package uk.gov.netz.api.workflow.request.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.netz.api.workflow.request.core.domain.RequestSequence;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.util.Optional;

public interface RequestSequenceRepository extends JpaRepository<RequestSequence, Long> {

    Optional<RequestSequence> findByRequestType(RequestType requestType);
    
    Optional<RequestSequence> findByBusinessIdentifierAndRequestType(String businessIdentifier, RequestType requestType);
}
