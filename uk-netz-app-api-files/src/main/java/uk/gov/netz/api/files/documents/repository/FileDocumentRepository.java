package uk.gov.netz.api.files.documents.repository;

import org.springframework.stereotype.Repository;
import uk.gov.netz.api.files.common.repository.FileEntityRepository;
import uk.gov.netz.api.files.documents.domain.FileDocument;

@Repository
public interface FileDocumentRepository extends FileEntityRepository<FileDocument, Long> {

    boolean existsByUuid(String uuid);
}
