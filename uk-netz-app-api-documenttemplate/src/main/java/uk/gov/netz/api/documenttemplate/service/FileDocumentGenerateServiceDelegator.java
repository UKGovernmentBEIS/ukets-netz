package uk.gov.netz.api.documenttemplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateFileInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;
import uk.gov.netz.api.files.documents.service.storage.FileDocumentStorageService;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FileDocumentGenerateServiceDelegator {
    
    private final DocumentTemplateFileService documentTemplateFileService;
    private final FileDocumentGenerateService fileDocumentGenerateService;
    private final FileDocumentStorageService fileDocumentStorageService;
    
    @Transactional
    public FileInfoDTO generateAndSaveFileDocument(String type, TemplateParams templateParams, String fileNameToGenerate) {
        byte[] generatedFile = doGenerate(type, templateParams, fileNameToGenerate);
        return fileDocumentStorageService.createFileDocument(generatedFile, fileNameToGenerate);
    }

    @Transactional
    public CompletableFuture<FileInfoDTO> generateAndSaveFileDocumentAsync(String type, TemplateParams templateParams, String fileNameToGenerate) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] generatedFile = doGenerate(type, templateParams, fileNameToGenerate);
            return fileDocumentStorageService.createFileDocument(generatedFile, fileNameToGenerate);
        });
    }
    
    @Transactional(readOnly = true)
    public FileDTO generateFileDocument(String type, TemplateParams templateParams, String fileNameToGenerate) {

        final byte[] generatedFile = doGenerate(type, templateParams, fileNameToGenerate);
        return FileDTO.builder()
            .fileContent(generatedFile)
            .fileName(fileNameToGenerate)
            .fileType(MimeTypeUtils.detect(generatedFile, fileNameToGenerate))
            .fileSize(generatedFile.length)
            .build();
    }
    
    @Transactional
	public String generateDocumentAsyncConvert(String type, TemplateParams templateParams,
			Map<String, String> documentMetadata) {
        return generateDocumentAsyncConvert(type, templateParams, documentMetadata, DocumentGenerationPriority.HIGH);
    }

    @Transactional
	public String generateDocumentAsyncConvert(String type, TemplateParams templateParams,
			Map<String, String> documentMetadata, DocumentGenerationPriority priority) {
    	final DocumentTemplateFileInfoDTO documentTemplateFile = findDocumentTemplateInfo(type, templateParams);
    	
    	if(!documentTemplateFile.isConvertRequired()) {
    		throw new IllegalArgumentException("Convert is required for document template type " + type);
    	}
    	
    	try {
			return fileDocumentGenerateService.generateFileDocumentFromTemplateAsyncConvert(documentTemplateFile,
					templateParams, documentMetadata, priority);
        } catch (DocumentTemplateProcessException e) {
			throw new BusinessException(ErrorCode.DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR,
					documentTemplateFile.getFile().getFileName());
        }
    }

    private byte[] doGenerate(String type, TemplateParams templateParams, String fileNameToGenerate) {
        // Get file document template
        final DocumentTemplateFileInfoDTO documentTemplateFile = findDocumentTemplateInfo(type, templateParams);

        try {
            // Generate file from template
            return fileDocumentGenerateService.generateFileDocumentFromTemplate(
                    documentTemplateFile, templateParams, fileNameToGenerate);
        } catch (DocumentTemplateProcessException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR, documentTemplateFile.getFile().getFileName());
        }
    }

	private DocumentTemplateFileInfoDTO findDocumentTemplateInfo(String type, TemplateParams templateParams) {
		return documentTemplateFileService
                .getFileDocumentTemplateFileInfoByTypeAndCompetentAuthority(
                        type,
                        templateParams.getCompetentAuthorityParams().getCompetentAuthority().getId()
                );
	}
    
}
