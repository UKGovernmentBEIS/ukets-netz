package uk.gov.netz.docgenerator.client.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DocumentGenerationRequestTest {

    @Test
    void builderDefaultsPriorityMetadataAndNormalize() {
        DocumentGenerationRequest request = DocumentGenerationRequest.builder()
            .docxBytes(new byte[] {1})
            .build();

        assertThat(request.getDocxBytes()).containsExactly(1);
        assertThat(request.getPriority()).isEqualTo(DocumentGenerationPriority.HIGH);
        assertThat(request.getMetadata()).isEmpty();
        assertThat(request.isNormalize()).isFalse();
    }

    @Test
    void builderSupportsPriorityMetadataAndNormalize() {
        DocumentGenerationRequest request = DocumentGenerationRequest.builder()
            .docxBytes(new byte[] {1})
            .priority(DocumentGenerationPriority.LOW)
            .metadata(Map.of("requestId", "request-1"))
            .normalize(true)
            .build();

        assertThat(request.getPriority()).isEqualTo(DocumentGenerationPriority.LOW);
        assertThat(request.getMetadata()).containsEntry("requestId", "request-1");
        assertThat(request.isNormalize()).isTrue();
    }
}
