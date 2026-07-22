package uk.gov.netz.api.documenttemplate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateFileInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.templateparams.AccountTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.CompetentAuthorityTemplateParams;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.storage.FileDocumentStorageService;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDocumentGenerateServiceDelegatorTest {
    private static final byte[] GENERATED_FILE_BYTES = "generated file content".getBytes();
    private static final String FILE_NAME_TO_GENERATE = "generatedFileName";
    private static final String TYPE = DocumentTemplateType.IN_RFI;
    private static final CompetentAuthorityDTO CA =
        CompetentAuthorityDTO.builder().id(CompetentAuthorityEnum.ENGLAND).build();

    @InjectMocks
    private FileDocumentGenerateServiceDelegator service;
    
    @Mock
    private DocumentTemplateFileService documentTemplateFileService;

    @Mock
    private FileDocumentGenerateService fileDocumentGenerateService;

    @Mock
    private FileDocumentStorageService fileDocumentStorageService;

    @Test
    void generateAndSaveFileDocument() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        
        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE))
            .thenReturn(GENERATED_FILE_BYTES);
        
        UUID uuid = UUID.randomUUID();
        FileInfoDTO persistedGeneratedFileInfo = FileInfoDTO.builder().name(FILE_NAME_TO_GENERATE).uuid(uuid.toString()).build();
        when(fileDocumentStorageService.createFileDocument(GENERATED_FILE_BYTES, FILE_NAME_TO_GENERATE))
            .thenReturn(persistedGeneratedFileInfo);
        
        //invoke
        FileInfoDTO result = service.generateAndSaveFileDocument(TYPE, templateParams, FILE_NAME_TO_GENERATE);
        
        //assert
        assertThat(result).isEqualTo(persistedGeneratedFileInfo);
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE);
        verify(fileDocumentStorageService).createFileDocument(GENERATED_FILE_BYTES, FILE_NAME_TO_GENERATE);
        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService, fileDocumentStorageService);
    }

    @Test
    void generateAndSaveFileDocument_throws_business_exception_when_generate_file_fails() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE))
            .thenThrow(new DocumentTemplateProcessException("process failed"));

        //invoke
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.generateAndSaveFileDocument(TYPE, templateParams, FILE_NAME_TO_GENERATE));

        //assert
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR);
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE);
        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService);
        verifyNoInteractions(fileDocumentStorageService);
    }

    @Test
    void generateFileDocument() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE))
            .thenReturn(GENERATED_FILE_BYTES);

        FileDTO actualFileDTO = service.generateFileDocument(TYPE, templateParams, FILE_NAME_TO_GENERATE);

        assertFileDTO(actualFileDTO);

        verify(documentTemplateFileService).getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplate(templateFile,
            templateParams, FILE_NAME_TO_GENERATE);

        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService);
        verifyNoInteractions(fileDocumentStorageService);
    }

    @Test
    void generateFileDocument_throws_business_exception_when_generate_file_fails() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams,
            FILE_NAME_TO_GENERATE)).thenThrow(new DocumentTemplateProcessException("process failed"));

        BusinessException be = assertThrows(BusinessException.class, () ->
                service.generateFileDocument(TYPE, templateParams, FILE_NAME_TO_GENERATE));

        //assert
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR);
        verify(documentTemplateFileService).getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());

        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService);
        verifyNoInteractions(fileDocumentStorageService);
    }


    @Test
    void generateAndSaveFileDocumentAsync() throws DocumentTemplateProcessException, ExecutionException, InterruptedException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);

        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE))
            .thenReturn(GENERATED_FILE_BYTES);

        UUID uuid = UUID.randomUUID();
        FileInfoDTO persistedGeneratedFileInfo = FileInfoDTO.builder().name(FILE_NAME_TO_GENERATE).uuid(uuid.toString()).build();
        when(fileDocumentStorageService.createFileDocument(GENERATED_FILE_BYTES, FILE_NAME_TO_GENERATE))
            .thenReturn(persistedGeneratedFileInfo);

        //invoke
        FileInfoDTO result = service.generateAndSaveFileDocumentAsync(TYPE, templateParams, FILE_NAME_TO_GENERATE).get();

        //assert
        assertThat(result).isEqualTo(persistedGeneratedFileInfo);
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE);
        verify(fileDocumentStorageService).createFileDocument(GENERATED_FILE_BYTES, FILE_NAME_TO_GENERATE);
        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService, fileDocumentStorageService);
    }

    @Test
    void generateAndSaveFileDocumentAsync_throws_business_exception_when_generate_file_fails() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .build();

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        when(fileDocumentGenerateService.generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE))
            .thenThrow(new DocumentTemplateProcessException("process failed"));

        //invoke
        ExecutionException be = assertThrows(ExecutionException.class, service.generateAndSaveFileDocumentAsync(TYPE, templateParams, FILE_NAME_TO_GENERATE)::get);

        //assert
        assertThat(be.getCause().getMessage()).isEqualTo(ErrorCode.DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR.getMessage());
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplate(templateFile, templateParams, FILE_NAME_TO_GENERATE);
        verifyNoMoreInteractions(documentTemplateFileService, fileDocumentGenerateService);
        verifyNoInteractions(fileDocumentStorageService);
    }
    
    @Test
    void generateDocumentAsyncConvert() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();
        Map<String, String> documentMetadata = Map.of(
				"key1", "val1"
				);

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .convertRequired(true)
                .build();
        
        String resultExpected = "jobId";

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);
        
		when(fileDocumentGenerateService.generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams,
				documentMetadata, DocumentGenerationPriority.HIGH)).thenReturn(resultExpected);
        
        //invoke
		String result = service.generateDocumentAsyncConvert(TYPE, templateParams,
				documentMetadata);
        
        //assert
        assertThat(result).isEqualTo(resultExpected);
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
		verify(fileDocumentGenerateService).generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams,
				documentMetadata, DocumentGenerationPriority.HIGH);
    }

    @Test
    void generateDocumentAsyncConvertWithPriority() throws DocumentTemplateProcessException {
        TemplateParams templateParams = getTemplateParams();
        Map<String, String> documentMetadata = Map.of(
                "key1", "val1"
                );

        DocumentTemplateFileInfoDTO templateFile = DocumentTemplateFileInfoDTO.builder()
                .file(getFileDTO())
                .convertRequired(true)
                .build();

        String resultExpected = "jobId";

        when(documentTemplateFileService.getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId()))
            .thenReturn(templateFile);

        when(fileDocumentGenerateService.generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams,
                documentMetadata, DocumentGenerationPriority.LOW)).thenReturn(resultExpected);

        String result = service.generateDocumentAsyncConvert(
                TYPE,
                templateParams,
                documentMetadata,
                DocumentGenerationPriority.LOW);

        assertThat(result).isEqualTo(resultExpected);
        verify(documentTemplateFileService)
            .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(TYPE, CA.getId());
        verify(fileDocumentGenerateService).generateFileDocumentFromTemplateAsyncConvert(templateFile, templateParams,
                documentMetadata, DocumentGenerationPriority.LOW);
    }

    private TemplateParams getTemplateParams() {
        return TemplateParams.builder()
            .competentAuthorityParams(CompetentAuthorityTemplateParams.builder()
                .competentAuthority(CA)
                .build())
            .accountParams(mock(AccountTemplateParams.class))
            .build();
    }

    private FileDTO getFileDTO() {
        return FileDTO.builder()
            .fileName("fileDocTemplate")
            .fileContent("some content".getBytes())
            .fileType("some type")
            .fileSize("some content".length())
            .build();
    }

    private void assertFileDTO(FileDTO actualFileDTO) {
        assertThat(actualFileDTO.getFileContent()).isEqualTo(GENERATED_FILE_BYTES);
        assertThat(actualFileDTO.getFileName()).isEqualTo(FILE_NAME_TO_GENERATE);
        assertThat(actualFileDTO.getFileType()).isEqualTo("text/plain");
        assertThat(actualFileDTO.getFileSize()).isEqualTo(22);
    }
}
