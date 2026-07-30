package uk.gov.netz.api.files.documents.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;
import uk.gov.netz.api.files.documents.domain.FileDocument;
import uk.gov.netz.api.files.documents.repository.FileDocumentRepository;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.token.UserFileTokenService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDocumentStorageServiceTest {

    @InjectMocks
    private FileDocumentStorageService service;

    @Mock
    private FileDocumentRepository fileDocumentRepository;

    @Mock
    private UserFileTokenService userFileTokenService;
    
    @Test
    void getFileDTO() {
        String uuid = UUID.randomUUID().toString();
        String name = "file document name";
        byte[] content = "cotnent".getBytes();
        FileDocument fileDocument = FileDocument.builder()
            .uuid(uuid)
            .fileName(name)
            .fileContent(content)
            .fileSize(content.length)
            .fileType("docx")
            .status(FileStatus.PENDING)
            .createdBy("user")
            .lastUpdatedOn(LocalDateTime.now())
            .build();
        
        when(fileDocumentRepository.findByUuid(uuid)).thenReturn(Optional.of(fileDocument));
        
        FileDTO result = service.getFileDTO(uuid);
        
        assertThat(result).isEqualTo(FileDTO.builder()
                .fileName(name).fileSize(content.length).fileType("docx").fileContent(content)
                .createdBy("user")
                .build());
        verify(fileDocumentRepository, times(1)).findByUuid(uuid);
    }
    
    @Test
    void getFileInfoDTO() {
        String uuid = UUID.randomUUID().toString();
        String name = "file document name";
        FileDocument fileDocument = FileDocument.builder()
            .uuid(uuid)
            .fileName(name)
            .lastUpdatedOn(LocalDateTime.now())
            .build();
        
        when(fileDocumentRepository.findByUuid(uuid)).thenReturn(Optional.of(fileDocument));
        
        FileInfoDTO result = service.getFileInfoDTO(uuid);
        
        assertThat(result).isEqualTo(FileInfoDTO.builder()
        		.name(name)
        		.uuid(uuid)
                .build());
        verify(fileDocumentRepository, times(1)).findByUuid(uuid);
    }

    @Test
    void generateGetFileDocumentToken() {
        String uuid = UUID.randomUUID().toString();
        
        FileToken expectedFileToken = FileToken.builder()
            .token("token")
            .tokenExpirationMinutes(10L)
            .build();

        when(fileDocumentRepository.existsByUuid(uuid)).thenReturn(true);
        when(userFileTokenService.generateGetFileToken(uuid))
            .thenReturn(expectedFileToken);

        FileToken result = service.generateGetFileDocumentToken(uuid);
        
        assertEquals(expectedFileToken, result);
        verify(userFileTokenService, times(1)).generateGetFileToken(uuid);
    }

    @Test
    void generateGetFileDocumentToken_not_found() {
        String uuid = UUID.randomUUID().toString();

        when(fileDocumentRepository.existsByUuid(uuid)).thenReturn(false);

        BusinessException be = assertThrows(BusinessException.class,
            () -> service.generateGetFileDocumentToken(uuid));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, be.getErrorCode());
        verify(fileDocumentRepository, times(1)).existsByUuid(uuid);
        verifyNoInteractions(userFileTokenService);
    }

    @Test
    void getFileDTOByToken() throws IOException {
        String token = "token";
        String fileUuid = "fileUuid";
        String filename = "filename";
        FileDocument fileDocument = FileDocument.builder()
            .fileName(filename)
            .fileContent(filename.getBytes())
            .fileSize(filename.length())
            .fileType("docx")
            .build();

        FileDTO expectedFileDTO = FileDTO.builder()
            .fileName(fileDocument.getFileName())
            .fileContent(fileDocument.getFileContent())
            .fileSize(fileDocument.getFileSize())
            .fileType(fileDocument.getFileType())
            .build();

        when(userFileTokenService.resolveGetFileUuid(token)).thenReturn(fileUuid);
        when(fileDocumentRepository.findByUuid(fileUuid)).thenReturn(Optional.of(fileDocument));

        FileDTO result = service.getFileDTOByToken(token);
        assertEquals(expectedFileDTO, result);

        verify(userFileTokenService, times(1)).resolveGetFileUuid(token);
        verify(fileDocumentRepository, times(1)).findByUuid(fileUuid);
    }
    
    @Test
    void createFileDocumentWithUuid() throws IOException {
    	String uuid = UUID.randomUUID().toString();
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "sample.pdf");
        byte[] fileContent = Files.readAllBytes(sampleFilePath);
        String fileName = "file document name.pdf";
        
        FileInfoDTO result = service.createFileDocumentWithUuid(fileContent, fileName, uuid);
        assertThat(result.getName()).isEqualTo(fileName);
        assertThat(result.getUuid()).isNotBlank();
        
        ArgumentCaptor<FileDocument> fileDocumentCaptor = ArgumentCaptor.forClass(FileDocument.class);
        verify(fileDocumentRepository, times(1)).save(fileDocumentCaptor.capture());
        FileDocument fileDocumentCaptured = fileDocumentCaptor.getValue();
        assertThat(fileDocumentCaptured.getFileName()).isEqualTo(fileName);
        assertThat(fileDocumentCaptured.getFileType()).isEqualTo(MimeTypeUtils.detect(fileContent, fileName));
        assertThat(fileDocumentCaptured.getFileSize()).isEqualTo(fileContent.length);
        assertThat(fileDocumentCaptured.getUuid()).isEqualTo(uuid);
        assertThat(fileDocumentCaptured.getStatus()).isEqualTo(FileStatus.SUBMITTED);
        
    }
    
    @Test
    void createFileDocument() throws IOException {
        Path sampleFilePath = Paths.get("src", "test", "resources", "files", "sample.pdf");
        byte[] fileContent = Files.readAllBytes(sampleFilePath);
        String fileName = "file document name.pdf";
        
        FileInfoDTO result = service.createFileDocument(fileContent, fileName);
        assertThat(result.getName()).isEqualTo(fileName);
        assertThat(result.getUuid()).isNotBlank();
        
        ArgumentCaptor<FileDocument> fileDocumentCaptor = ArgumentCaptor.forClass(FileDocument.class);
        verify(fileDocumentRepository, times(1)).save(fileDocumentCaptor.capture());
        FileDocument fileDocumentCaptured = fileDocumentCaptor.getValue();
        assertThat(fileDocumentCaptured.getFileName()).isEqualTo(fileName);
        assertThat(fileDocumentCaptured.getFileSize()).isEqualTo(fileContent.length);
        assertThat(fileDocumentCaptured.getFileType()).isEqualTo(MimeTypeUtils.detect(fileContent, fileName));
        assertThat(fileDocumentCaptured.getUuid()).isNotBlank();
        assertThat(fileDocumentCaptured.getStatus()).isEqualTo(FileStatus.SUBMITTED);
    }
    
}