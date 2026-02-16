package uk.gov.netz.api.mireport.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

@Repository
public interface MiReportSystemRepository extends JpaRepository<MiReportSystemEntity, Integer> {

    @Transactional(readOnly = true)
    List<MiReportSystemSearchResult> findByCompetentAuthority(CompetentAuthorityEnum competentAuthority);
}
