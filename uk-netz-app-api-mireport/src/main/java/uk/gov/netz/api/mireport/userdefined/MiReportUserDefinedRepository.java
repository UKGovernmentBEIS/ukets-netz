package uk.gov.netz.api.mireport.userdefined;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Optional;

@Repository
public interface MiReportUserDefinedRepository extends JpaRepository<MiReportUserDefinedEntity, Long> {

	@Transactional(readOnly = true)
	Page<MiReportUserDefinedEntity> findAllByCompetentAuthority(CompetentAuthorityEnum competentAuthority, Pageable pageable);

	@Query(name = MiReportUserDefinedEntity.NAMED_QUERY_FIND_BY_REPORT_NAME_AND_CA)
	Optional<Long> findIdByReportNameAndCA(String reportName, CompetentAuthorityEnum competentAuthority);
}
