package uk.gov.netz.api.mireport.userdefined.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiReportUserDefinedCategoryRepository extends JpaRepository<MiReportUserDefinedCategoryEntity, Long> {

    List<MiReportUserDefinedCategoryEntity> findAllByEnabledTrueOrderByNameAsc();

}
