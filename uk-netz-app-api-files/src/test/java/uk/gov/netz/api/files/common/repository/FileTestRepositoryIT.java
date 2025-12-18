package uk.gov.netz.api.files.common.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.FileTest;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class FileTestRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private FileTestRepository fileTestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void existsByUuid() {
        String uuid1 = UUID.randomUUID().toString();
        createFile("file1", uuid1, FileStatus.PENDING);

        final boolean actual = fileTestRepository.existsByUuid(uuid1);

        assertTrue(actual);
    }

    @Test
    void existsByUuid_false() {
        String uuid1 = UUID.randomUUID().toString();
        createFile("file1", uuid1, FileStatus.PENDING);

        final boolean actual = fileTestRepository.existsByUuid(UUID.randomUUID().toString());

        assertFalse(actual);
    }

    @Test
    void getFileNamesByUuid() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.SUBMITTED);

        final List<FileInfoDTO> actual = fileTestRepository.getFileInfoByUuids(Set.of(uuid1, uuid2));

        assertThat(actual).extracting(FileInfoDTO::getName).containsExactlyInAnyOrder(fileName1, fileName2);
        assertThat(actual).extracting(FileInfoDTO::getUuid).containsExactlyInAnyOrder(uuid1, uuid2);
    }

    @Test
    void getFileNamesByUuid_one_result() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.SUBMITTED);

        final List<FileInfoDTO> actual = fileTestRepository.getFileInfoByUuids(Set.of(uuid1, UUID.randomUUID().toString()));

        assertThat(actual).extracting(FileInfoDTO::getName).containsExactlyInAnyOrder(fileName1);
        assertThat(actual).extracting(FileInfoDTO::getUuid).containsExactlyInAnyOrder(uuid1);
    }

    @Test
    void updateFilesStatusByUuid() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.PENDING);

        fileTestRepository.updateFilesStatusByUuids(Set.of(uuid1, uuid2), FileStatus.SUBMITTED);
        flushAndClear();

        final Optional<FileTest> file1 = fileTestRepository.findByUuid(uuid1);
        final Optional<FileTest> file2 = fileTestRepository.findByUuid(uuid2);

        assertThat(file1).isPresent();
        assertThat(file1.get().getStatus()).isEqualTo(FileStatus.SUBMITTED);
        assertThat(file2).isPresent();
        assertThat(file2.get().getStatus()).isEqualTo(FileStatus.SUBMITTED);
    }

    @Test
    void updateFilesStatusByUuidSpecificFiles() {
        // Given
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String uuid3 = UUID.randomUUID().toString();
        createFile("file1", uuid1, FileStatus.PENDING);
        createFile("file2", uuid2, FileStatus.PENDING);
        createFile("file3", uuid3, FileStatus.PENDING);

        fileTestRepository.updateFilesStatusByUuids(Set.of(uuid1, uuid2), FileStatus.SUBMITTED);
        flushAndClear();

        // Then
        final Optional<FileTest> file1 = fileTestRepository.findByUuid(uuid1);
        final Optional<FileTest> file2 = fileTestRepository.findByUuid(uuid2);
        final Optional<FileTest> file3 = fileTestRepository.findByUuid(uuid3);

        assertThat(file1.get().getStatus()).isEqualTo(FileStatus.SUBMITTED);
        assertThat(file2.get().getStatus()).isEqualTo(FileStatus.SUBMITTED);
        assertThat(file3.get().getStatus()).isEqualTo(FileStatus.PENDING);
    }

    @Test
    void deleteFilesByUuid() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.SUBMITTED);

        fileTestRepository.deleteFilesByUuids(Set.of(uuid1));
        flushAndClear();

        final Optional<FileTest> file1 = fileTestRepository.findByUuid(uuid1);
        final Optional<FileTest> file2 = fileTestRepository.findByUuid(uuid2);

        assertThat(file1).isEmpty();
        assertThat(file2).isPresent();
        assertThat(file2.get().getUuid()).isEqualTo(uuid2);

    }

    @Test
    void deleteFilesByStatusAndDateBefore() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.PENDING);

        fileTestRepository.deleteFilesByStatusAndLastUpdatedDateBefore(FileStatus.PENDING, LocalDateTime.now());
        flushAndClear();

        final Optional<FileTest> file1 = fileTestRepository.findByUuid(uuid1);
        final Optional<FileTest> file2 = fileTestRepository.findByUuid(uuid2);

        assertThat(file1).isEmpty();
        assertThat(file2).isEmpty();

    }

    private void createFile(String name, String uuid, FileStatus status) {
        final FileTest file = FileTest.builder()
                .fileName(name)
                .uuid(uuid)
                .fileSize(1L)
                .fileContent(new byte[1])
                .fileType("type")
                .createdBy("system")
                .status(status)
                .lastUpdatedOn(LocalDateTime.now().minusDays(1))
                .build();
        entityManager.persist(file);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
