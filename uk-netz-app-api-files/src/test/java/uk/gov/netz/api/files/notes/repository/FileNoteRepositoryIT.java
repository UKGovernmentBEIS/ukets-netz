package uk.gov.netz.api.files.notes.repository;

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
import uk.gov.netz.api.files.notes.domain.FileNote;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
public class FileNoteRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private FileNoteRepository fileNoteRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deleteFilesByStatusAndLastUpdatedDateBefore() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String fileName1 = "file1";
        String fileName2 = "file2";
        createFile(fileName1, uuid1, FileStatus.PENDING);
        createFile(fileName2, uuid2, FileStatus.PENDING);

        fileNoteRepository.deleteFilesByStatusAndLastUpdatedDateBefore(FileStatus.PENDING, LocalDateTime.now());
        flushAndClear();

        final Optional<FileNote> file1 = fileNoteRepository.findByUuid(uuid1);
        final Optional<FileNote> file2 = fileNoteRepository.findByUuid(uuid2);

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
