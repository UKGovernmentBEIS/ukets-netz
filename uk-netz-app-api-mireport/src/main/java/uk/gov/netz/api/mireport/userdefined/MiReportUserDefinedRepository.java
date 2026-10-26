package uk.gov.netz.api.mireport.userdefined;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MiReportUserDefinedRepository extends JpaRepository<MiReportUserDefinedEntity, Long> {

	@Transactional(readOnly = true)
	@Query(value = """
			select distinct r from MiReportUserDefinedEntity r
			left join r.categories c
			where (:categoryId is null or c.id = :categoryId)
			  and (:term is null
				   or lower(r.reportName) like :term escape '\\'
				   or lower(r.description) like :term escape '\\')
			  and (:userId is null
				   or exists (select 1 from MiReportUserDefinedFavouriteEntity f
							  where f.miReportId = r.id and f.userId = :userId))""",
			countQuery = """
			select count(distinct r) from MiReportUserDefinedEntity r
			left join r.categories c
			where (:categoryId is null or c.id = :categoryId)
			  and (:term is null
				   or lower(r.reportName) like :term escape '\\'
				   or lower(r.description) like :term escape '\\')
			  and (:userId is null
				   or exists (select 1 from MiReportUserDefinedFavouriteEntity f
							  where f.miReportId = r.id and f.userId = :userId))""")
	Page<MiReportUserDefinedEntity> findAllBySearchCriteria(
			@Param("categoryId") Long categoryId,
			@Param("term") String term,
			@Param("userId") String userId,
			Pageable pageable);

	@Query("SELECT r.id FROM MiReportUserDefinedEntity r WHERE r.reportName = :reportName")
	Optional<Long> findIdByReportName(String reportName);
}
