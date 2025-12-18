package uk.gov.netz.api.files.common.repository;

import org.springframework.stereotype.Repository;
import uk.gov.netz.api.files.common.domain.FileTest;

@Repository
public interface FileTestRepository extends FileEntityRepository<FileTest, Long> {
}
