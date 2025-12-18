package uk.gov.netz.api.files.attachments.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.repository.FileEntityRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface FileAttachmentRepository extends FileEntityRepository<FileAttachment, Long> {
    
    @Transactional(readOnly = true)
    List<FileAttachment> findByStatus(FileStatus status);
    
    @Transactional(readOnly = true)
    long countAllByUuidIn(Set<String> uuids);

    @Transactional(readOnly = true)
    List<FileAttachment> findAllByUuidIn(Set<String> uuids);

    @Transactional(readOnly = true)
    @Query("SELECT f.uuid FROM FileAttachment f WHERE f.uuid IN (:uuids)")
    Set<String> findExistingUuidsByUuidIn(Set<String> uuids);

    @Transactional(readOnly = true)
    @Query("SELECT f.uuid FROM FileAttachment f WHERE f.uuid IN (:uuids) and f.status = :status")
    Set<String> findExistingUuidsByUuidInAndStatus(Set<String> uuids, FileStatus status);
    
    @Transactional(readOnly = true)
    Stream<FileAttachment> streamAllByUuidIn(Set<String> uuids);

    @Modifying
    @Transactional
    @Query("UPDATE FileAttachment f SET f.status = :status WHERE f.uuid IN (:uuids)")
    void updateStatusInUuids(FileStatus status, List<String> uuids);
}
