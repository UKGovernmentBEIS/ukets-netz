package uk.gov.netz.api.documenttemplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.documenttemplate.config.DocumentGeneratorProperties;
import uk.gov.netz.api.documenttemplate.config.RestEndPointEnum;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentParameters;
import uk.gov.netz.api.restclient.RestClientApi;
import uk.gov.netz.docgenerator.client.DocumentGeneratorClient;
import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;

@Log4j2
@Service
@RequiredArgsConstructor
public class DocumentGeneratorRemoteClientService implements DocumentGeneratorClientService {

    private final RestTemplate restTemplate;
    private final DocumentGeneratorProperties properties;
    private final DocumentGeneratorClient documentGeneratorClient;

    @Override
    public byte[] generateDocument(byte[] source, String outputFilename) {
        return generateDocument(source, outputFilename, false);
    }

    @Override
    public byte[] generateDocument(byte[] source, String outputFilename, boolean normalize) {
        RestClientApi appRestApi = buildApiCall(
                RestEndPointEnum.GENERATE,
                source,
                DocumentParameters.builder()
                        .outputFilename(outputFilename)
                        .normalize(normalize)
                        .build()
        );

        try {
            final ResponseEntity<byte[]> res = appRestApi.performApiCall();
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER, e.getMessage());
        }
    }

    @Override
    public String generateDocumentAsync(byte[] source, Map<String, String> documentMetadata, boolean normalize) throws Exception {
        return generateDocumentAsync(source, documentMetadata, normalize, DocumentGenerationPriority.HIGH);
    }

    @Override
    public String generateDocumentAsync(
            byte[] source,
            Map<String, String> documentMetadata,
            boolean normalize,
            DocumentGenerationPriority priority) throws Exception {
        DocumentGenerationRequest request = DocumentGenerationRequest.builder()
                .docxBytes(source)
                .metadata(documentMetadata)
                .normalize(normalize)
                .priority(priority)
                .build();
        AsyncJobReceipt receipt = documentGeneratorClient.submitAsync(request);
        return receipt.getJobId();
    }

	private RestClientApi buildApiCall(RestEndPointEnum endpoint, byte[] source, DocumentParameters parameters) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(source) {
            @Override
            public String getFilename() {
                return "source";
            }
        });
		body.add("parameters", parameters);

		return RestClientApi.builder()
				.uri(UriComponentsBuilder
						.fromUriString(properties.getUrl())
						.path(endpoint.getPath())
						.build()
						.toUri())
				.restEndPoint(endpoint)
				.headers(headers)
				.restTemplate(restTemplate)
				.body(body)
				.build();
	}

}
