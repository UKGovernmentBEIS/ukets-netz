package uk.gov.netz.docgenerator.client.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JobMessageTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void deserializesOmittedNormalizeAsFalseAndOmittedMetadataAsEmptyMap() throws Exception {
        String json = """
            {"jobId":"job-1"}
            """;

        JobMessage message = OBJECT_MAPPER.readValue(json, JobMessage.class);

        assertThat(message.getJobId()).isEqualTo("job-1");
        assertThat(message.isNormalize()).isFalse();
        assertThat(message.getMetadata()).isEmpty();
    }

    @Test
    void deserializesNormalizeWhenPresent() throws Exception {
        String json = """
            {"jobId":"job-1","normalize":true}
            """;

        JobMessage message = OBJECT_MAPPER.readValue(json, JobMessage.class);

        assertThat(message.isNormalize()).isTrue();
    }

    @Test
    void serializesEmptyMetadataAndFalseNormalizeAsOmitted() throws Exception {
        JobMessage message = new JobMessage("job-1");

        String json = OBJECT_MAPPER.writeValueAsString(message);

        assertThat(json).isEqualTo("{\"jobId\":\"job-1\"}");
    }

    @Test
    void serializesNormalizeWhenTrue() throws Exception {
        JobMessage message = new JobMessage("job-1", true);

        String json = OBJECT_MAPPER.writeValueAsString(message);

        assertThat(json).isEqualTo("{\"jobId\":\"job-1\",\"normalize\":true}");
    }

    @Test
    void serializesMetadataAndNormalizeWhenPresent() throws Exception {
        JobMessage message = new JobMessage("job-1", Map.of("requestId", "request-1"), true);

        String json = OBJECT_MAPPER.writeValueAsString(message);

        assertThat(json)
            .contains("\"jobId\":\"job-1\"")
            .contains("\"normalize\":true")
            .contains("\"metadata\":{\"requestId\":\"request-1\"}");
    }

    @Test
    void copiesMetadataOnConstruction() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("requestId", "request-1");

        JobMessage message = new JobMessage("job-1", metadata, true);
        metadata.put("requestId", "request-2");

        assertThat(message.getMetadata()).containsEntry("requestId", "request-1");
        assertThat(message.isNormalize()).isTrue();
    }
}
