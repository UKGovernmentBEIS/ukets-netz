package uk.gov.netz.api.mireport.userdefined.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MiReportUserDefinedHistoryRepository extends JpaRepository<MiReportUserDefinedHistoryEntity, Long> {

    @Transactional(readOnly = true)
    Page<MiReportUserDefinedHistoryEntity> findByMiReportId(Long miReportId, Pageable pageable);

}
