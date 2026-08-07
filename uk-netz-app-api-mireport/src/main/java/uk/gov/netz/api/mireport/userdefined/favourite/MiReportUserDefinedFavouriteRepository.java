package uk.gov.netz.api.mireport.userdefined.favourite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MiReportUserDefinedFavouriteRepository extends JpaRepository<MiReportUserDefinedFavouriteEntity, Long> {

    @Transactional(readOnly = true)
    boolean existsByUserIdAndMiReportId(String userId, Long miReportId);

    @Modifying
    @Transactional
    void deleteByUserIdAndMiReportId(String userId, Long miReportId);
}
