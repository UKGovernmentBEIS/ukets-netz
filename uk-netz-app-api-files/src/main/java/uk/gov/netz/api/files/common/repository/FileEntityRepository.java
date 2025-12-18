package uk.gov.netz.api.files.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.files.common.domain.FileEntity;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@NoRepositoryBean
public interface FileEntityRepository<T extends FileEntity, ID extends Serializable> extends JpaRepository<T, ID> {

    Optional<T> findByUuid(String uuid);
    
    @Modifying
    @Transactional
    void deleteByUuidIn(List<String> uuids);

    boolean existsByUuid(String uuid);

    @Transactional(readOnly = true)
    @Query("select new uk.gov.netz.api.files.common.domain.dto.FileInfoDTO(fileEntity.fileName, fileEntity.uuid) " +
            "from #{#entityName} fileEntity " +
            "where fileEntity.uuid in (:uuids) ")
    List<FileInfoDTO> getFileInfoByUuids(Set<String> uuids);

    @Transactional
    @Modifying
    @Query( "update #{#entityName} fileEntity " +
            "set fileEntity.status = :status " +
            "where fileEntity.uuid in (:uuids)")
    void updateFilesStatusByUuids(Set<String> uuids, FileStatus status);

    @Transactional
    @Modifying
    @Query(  "delete from #{#entityName} fileEntity " +
            "where fileEntity.uuid in (:uuids)")
    void deleteFilesByUuids(Set<String> uuids);

    @Transactional
    @Modifying
    @Query(  "delete from #{#entityName} fileEntity " +
            "where fileEntity.status = :status " +
            "and fileEntity.lastUpdatedOn < :date")
    void deleteFilesByStatusAndLastUpdatedDateBefore(FileStatus status, LocalDateTime date);

}