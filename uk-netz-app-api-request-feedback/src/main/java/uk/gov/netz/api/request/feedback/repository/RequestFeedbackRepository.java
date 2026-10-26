package uk.gov.netz.api.request.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.request.feedback.domain.RequestFeedback;

import java.util.Optional;

@Repository
public interface RequestFeedbackRepository extends JpaRepository<RequestFeedback, Long> {

    Optional<RequestFeedback> findByRequestIdAndType(String requestId, String type);

    boolean existsByRequestIdAndType(String requestId, String type);
}