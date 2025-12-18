package uk.gov.netz.api.files.documents.repository;

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
import uk.gov.netz.api.files.documents.domain.FileDocument;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class FileDocumentRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private FileDocumentRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void existsByUuid() {
        String uuid = UUID.randomUUID().toString();
        boolean result = repo.existsByUuid(uuid);
        assertThat(result).isFalse();
        
        FileDocument fileDocument = FileDocument.builder()
                .uuid(uuid)
                .fileName("filename")
                .fileContent("filename".getBytes())
                .fileSize("filename".length())
                .fileType("txt")
                .status(FileStatus.SUBMITTED)
                .createdBy("user")
                .lastUpdatedOn(LocalDateTime.now())
                .build();

        entityManager.persist(fileDocument);
        
        flushAndClear();

        result = repo.existsByUuid(uuid);
        
        assertThat(result).isTrue();
    }
    
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
    
}
