package uk.gov.netz.api.documenttemplate.service;

import java.util.Map;

import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;

public interface DocumentGeneratorClientService {

    byte[] generateDocument(byte[] source, String fileNameToGenerate) throws Exception;

    default byte[] generateDocument(byte[] source, String fileNameToGenerate, boolean normalize) throws Exception {
        return generateDocument(source, fileNameToGenerate);
    }

    String generateDocumentAsync(byte[] source, Map<String, String> documentMetadata, boolean normalize) throws Exception;

    default String generateDocumentAsync(
            byte[] source,
            Map<String, String> documentMetadata,
            boolean normalize,
            DocumentGenerationPriority priority) throws Exception {
        return generateDocumentAsync(source, documentMetadata, normalize);
    }

}
