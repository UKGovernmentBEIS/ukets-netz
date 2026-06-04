package uk.gov.netz.api.referencedata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.referencedata.domain.County;

/**
 * Repository for {@link County} objects.
 */
@Repository
public interface CountyRepository extends JpaRepository<County, Long> {
}
