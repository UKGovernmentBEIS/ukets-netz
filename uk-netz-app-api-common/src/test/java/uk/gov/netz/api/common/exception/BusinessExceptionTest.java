package uk.gov.netz.api.common.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void noDataConstructor_leavesEmptyDataArray() {
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);

        assertThat(exception.getData()).isEmpty();
    }

    @Test
    void noDataConstructor_serializesToEmptyJsonArray() throws Exception {
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);

        JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(exception.getData()));

        assertThat(json.isArray()).isTrue();
        assertThat(json).isEmpty();
    }
}
