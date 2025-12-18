package uk.gov.netz.api.files.attachments.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.common.domain.FileStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class FileAttachmentRepositoryIT extends AbstractContainerBaseTest {
    
    @Autowired
    private FileAttachmentRepository repo;
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void persistAndGet() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "sample.pdf");
        String uuid = UUID.randomUUID().toString();
        FileAttachment attachment = createAttachment(sampleFilePath, uuid);
        entityManager.persist(attachment);
        
        flushAndClear();

        assertThat(repo.findById(attachment.getId())).contains(attachment);
    }
    
    @Test
    void findByUuid() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "sample.pdf");
        
        String uuid = UUID.randomUUID().toString();
        FileAttachment attachment = createAttachment(sampleFilePath, uuid);
        entityManager.persist(attachment);
        
        flushAndClear();
        
        assertThat(repo.findByUuid(uuid)).contains(attachment);
    }

    private FileAttachment createAttachment(Path sampleFilePath, String uuid) throws IOException {
        byte[] fileContent = Files.readAllBytes(sampleFilePath);
        return FileAttachment.builder()
                .uuid(uuid)
                .fileName(sampleFilePath.getFileName().toString())
                .fileContent(fileContent)
                .fileSize(Files.size(sampleFilePath))
                .fileType(Files.probeContentType(sampleFilePath))
                .status(FileStatus.PENDING)
                .createdBy("user")
                .lastUpdatedOn(LocalDateTime.now())
                .build();
    }
    
    

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
