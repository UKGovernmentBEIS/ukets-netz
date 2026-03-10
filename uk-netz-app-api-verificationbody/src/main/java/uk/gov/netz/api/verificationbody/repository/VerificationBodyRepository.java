package uk.gov.netz.api.verificationbody.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyNameInfoDTO;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VerificationBodyRepository extends JpaRepository<VerificationBody, Long> {

    @EntityGraph(value = "emissionTradingSchemes-graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query(name = VerificationBody.NAMED_QUERY_FIND_BY_ID)
    Optional<VerificationBody> findByIdEagerEmissionTradingSchemes(Long id);

    @Transactional(readOnly = true)
    Optional<VerificationBody> findByIdAndStatus(Long id, VerificationBodyStatus status);

    @Transactional(readOnly = true)
    @Query(name = VerificationBody.NAMED_QUERY_FIND_ACTIVE_VER_BODIES_ACCREDITED_TO_EMISSION_TRADING_SCHEME)
    List<VerificationBodyNameInfoDTO> findActiveVerificationBodiesAccreditedToEmissionTradingScheme(String emissionTradingScheme);

    @Transactional(readOnly = true)
    boolean existsByIdAndStatus(Long id, VerificationBodyStatus status);

    @Transactional(readOnly = true)
    boolean existsByIdAndStatusNot(Long id, VerificationBodyStatus status);

    @Transactional(readOnly = true)
    List<VerificationBody> findAllByIdIn(Set<Long> ids);
    
    @Transactional(readOnly = true)
    @Query(name = VerificationBody.NAMED_QUERY_IS_VER_BODY_ACCREDITED_TO_EMISSION_TRADING_SCHEME)
    boolean isVerificationBodyAccreditedToEmissionTradingScheme(Long vbId, String emissionTradingScheme);

    @Transactional(readOnly = true)
    Optional<VerificationBody> findByName(String name);

    @Transactional(readOnly = true)
    List<VerificationBody> findByIdNot(Long id);
}
