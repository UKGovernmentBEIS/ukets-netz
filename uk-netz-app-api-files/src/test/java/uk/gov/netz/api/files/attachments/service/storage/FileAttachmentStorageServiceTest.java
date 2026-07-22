package uk.gov.netz.api.files.attachments.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.attachments.repository.FileAttachmentRepository;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.token.UserFileTokenService;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAttachmentStorageServiceTest {
    
    @InjectMocks
    private FileAttachmentStorageService service;
    
    @Mock
    private FileAttachmentRepository fileAttachmentRepository;
    
    @Mock
    private UserFileTokenService userFileTokenService;
    
    @Test
    void generateGetFileAttachmentToken() {
        String attachmentUuid = "attachmentUuid";
        FileToken fileToken = FileToken.builder()
                .token("roken")
                .tokenExpirationMinutes(1l)
                .build();
        when(fileAttachmentRepository.existsByUuid(attachmentUuid)).thenReturn(true);
        when(userFileTokenService.generateGetFileToken(attachmentUuid))
            .thenReturn(fileToken);
        
        FileToken result = service.generateGetFileAttachmentToken(attachmentUuid);
        assertThat(result).isEqualTo(fileToken);
        verify(fileAttachmentRepository, times(1)).existsByUuid(attachmentUuid);
        verify(userFileTokenService, times(1)).generateGetFileToken(attachmentUuid);
    }
    
    @Test
    void generateGetFileAttachmentToken_attachment_not_found() {
        String attachmentUuid = "attachmentUuid";
        when(fileAttachmentRepository.existsByUuid(attachmentUuid)).thenReturn(false);
        
        BusinessException be = assertThrows(BusinessException.class, () -> {
            service.generateGetFileAttachmentToken(attachmentUuid);
        });
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        
        verify(fileAttachmentRepository, times(1)).existsByUuid(attachmentUuid);
        verifyNoInteractions(userFileTokenService);
    }
    
    @Test
    void getFileDTOByToken() throws IOException {
        String getFileAttachmentToken = "token";
        String fileAttachmentUuid = "fileAttachmentUuid";
        FileAttachment fileAttachment = FileAttachment.builder()
                .fileName("file")
                .fileContent("content".getBytes())
                .fileSize(1l)
                .fileType("type")
                .build();
        
        when(userFileTokenService.resolveGetFileUuid(getFileAttachmentToken))
            .thenReturn(fileAttachmentUuid);
        when(fileAttachmentRepository.findByUuid(fileAttachmentUuid))
            .thenReturn(Optional.of(fileAttachment));
        
        FileDTO result = service.getFileDTOByToken(getFileAttachmentToken);
        assertThat(result.getFileContent()).isEqualTo(fileAttachment.getFileContent());
        assertThat(result.getFileName()).isEqualTo(fileAttachment.getFileName());
        assertThat(result.getFileSize()).isEqualTo(fileAttachment.getFileSize());
        assertThat(result.getFileType()).isEqualTo(fileAttachment.getFileType());
        
        verify(userFileTokenService, times(1)).resolveGetFileUuid(getFileAttachmentToken);
        verify(fileAttachmentRepository, times(1)).findByUuid(fileAttachmentUuid);
    }
    
}
