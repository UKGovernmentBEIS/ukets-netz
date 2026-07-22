package uk.gov.netz.api.documenttemplate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateFileInfoDTO;
import uk.gov.netz.api.documenttemplate.repository.DocumentTemplateRepository;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.FileDocumentTemplateService;
import uk.gov.netz.api.files.documents.service.FileDocumentTemplateTokenService;
import uk.gov.netz.api.token.FileToken;

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
class DocumentTemplateFileServiceTest {

    @InjectMocks
    private DocumentTemplateFileService service;
    
    @Mock
    private DocumentTemplateQueryService documentTemplateQueryService;
    
    @Mock
    private DocumentTemplateRepository documentTemplateRepository;

    @Mock
    private FileDocumentTemplateService fileDocumentTemplateService;

    @Mock
    private FileDocumentTemplateTokenService fileDocumentTemplateTokenService;

    @Test
    void generateGetFileDocumentTemplateToken() {
        Long documentTemplateId = 1L;
        Long fileDocumentTemplateId = 2L;
        UUID fileUuid = UUID.randomUUID();
        
        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .id(documentTemplateId)
                .fileDocumentTemplateId(fileDocumentTemplateId)
                .build();
        
        FileInfoDTO fileInfoDTO = FileInfoDTO.builder()
                .uuid(fileUuid.toString())
                .build();
        
        FileToken expectedFileToken = FileToken.builder()
            .token("token")
            .tokenExpirationMinutes(10L)
            .build();

        when(documentTemplateQueryService.getDocumentTemplateById(documentTemplateId)).thenReturn(documentTemplate);
        when(fileDocumentTemplateService.getFileInfoDocumentTemplateById(fileDocumentTemplateId)).thenReturn(fileInfoDTO);
        when(fileDocumentTemplateTokenService.generateGetFileDocumentTemplateToken(fileUuid.toString()))
            .thenReturn(expectedFileToken);

        FileToken result = service.generateGetFileDocumentTemplateToken(documentTemplateId, fileUuid);
        assertEquals(expectedFileToken, result);
        
        verify(documentTemplateQueryService, times(1)).getDocumentTemplateById(documentTemplateId);
        verify(fileDocumentTemplateService, times(1)).getFileInfoDocumentTemplateById(fileDocumentTemplateId);
        verify(fileDocumentTemplateTokenService, times(1)).generateGetFileDocumentTemplateToken(fileUuid.toString());
    }

    @Test
    void generateGetFileDocumentTemplateToken_file_uuid_not_match() {
        Long documentTemplateId = 1L;
        Long fileDocumentTemplateId = 2L;
        UUID fileUuid = UUID.randomUUID();
        UUID anotherFileUuid = UUID.randomUUID();
        
        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .id(documentTemplateId)
                .fileDocumentTemplateId(fileDocumentTemplateId)
                .build();
        
        FileInfoDTO fileInfoDTO = FileInfoDTO.builder()
                .uuid(anotherFileUuid.toString())
                .build();
        
        when(documentTemplateQueryService.getDocumentTemplateById(documentTemplateId)).thenReturn(documentTemplate);
        when(fileDocumentTemplateService.getFileInfoDocumentTemplateById(fileDocumentTemplateId)).thenReturn(fileInfoDTO);

        BusinessException be = assertThrows(BusinessException.class,
                () -> service.generateGetFileDocumentTemplateToken(documentTemplateId, fileUuid));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_TEMPLATE_FILE_NOT_FOUND);
        
        verify(documentTemplateQueryService, times(1)).getDocumentTemplateById(documentTemplateId);
        verify(fileDocumentTemplateService, times(1)).getFileInfoDocumentTemplateById(fileDocumentTemplateId);
        verifyNoInteractions(fileDocumentTemplateTokenService);
    }

    @Test
    void getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority() {
        final String type = DocumentTemplateType.IN_RFI;
        final CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        final Long fileDocumentTemplateId = 1L;
        final DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .processRequired(true)
                .convertRequired(true)
                .fileDocumentTemplateId(fileDocumentTemplateId)
                .build();
        final FileDTO file = FileDTO.builder().fileName("file").build();

        final DocumentTemplateFileInfoDTO expected = DocumentTemplateFileInfoDTO.builder()
                .processRequired(true)
                .convertRequired(true)
                .file(file)
                .build();

        when(documentTemplateRepository.findByTypeAndCompetentAuthority(type, competentAuthority))
                .thenReturn(Optional.of(documentTemplate));
        when(fileDocumentTemplateService.getFileDocumentTemplateById(fileDocumentTemplateId))
                .thenReturn(file);

        DocumentTemplateFileInfoDTO result = service
                .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(type, competentAuthority);

        assertThat(result).isEqualTo(expected);
        verify(documentTemplateRepository, times(1)).findByTypeAndCompetentAuthority(type, competentAuthority);
        verify(fileDocumentTemplateService, times(1)).getFileDocumentTemplateById(fileDocumentTemplateId);
    }
    
    @Test
    void getFileDocumentTemplateByTypeAndCompetentAuthority() {
        String type = DocumentTemplateType.IN_RFI;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        Long documentTemplateId = 1L;
        Long fileDocumentTemplateId = 2L;
        DocumentTemplate documentTemplate = DocumentTemplate.builder()
                .id(documentTemplateId)
                .fileDocumentTemplateId(fileDocumentTemplateId)
                .build();
        
        when(documentTemplateRepository.findByTypeAndCompetentAuthority(type, competentAuthority))
            .thenReturn(Optional.of(documentTemplate));
        
        FileDTO fileDocumentTemplate = FileDTO.builder()
                .fileName("fileDocTemplate")
                .fileContent("some content".getBytes())
                .fileType("some type")
                .fileSize("some content".length())
                .build();
        when(fileDocumentTemplateService.getFileDocumentTemplateById(fileDocumentTemplateId))
            .thenReturn(fileDocumentTemplate);
        
        FileDTO result = service.getFileDocumentTemplateByTypeAndCompetentAuthority(type, competentAuthority);
        assertThat(result).isEqualTo(fileDocumentTemplate);
        verify(documentTemplateRepository, times(1))
            .findByTypeAndCompetentAuthority(type, competentAuthority);
        verify(fileDocumentTemplateService, times(1)).getFileDocumentTemplateById(fileDocumentTemplateId);
    }
    
    @Test
    void getFileDocumentTemplateByTypeAndCompetentAuthority_not_found() {
        String type = DocumentTemplateType.IN_RFI;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        when(documentTemplateRepository.findByTypeAndCompetentAuthority(type, competentAuthority))
            .thenReturn(Optional.empty());
        
        BusinessException be = assertThrows(BusinessException.class,
                () -> service.getFileDocumentTemplateByTypeAndCompetentAuthority(type, competentAuthority));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        
        verify(documentTemplateRepository, times(1))
            .findByTypeAndCompetentAuthority(type, competentAuthority);
        verifyNoInteractions(fileDocumentTemplateService);
    }
}
