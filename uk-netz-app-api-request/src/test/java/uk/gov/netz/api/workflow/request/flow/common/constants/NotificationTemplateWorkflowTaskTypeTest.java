package uk.gov.netz.api.workflow.request.flow.common.constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateWorkflowTaskTypeTest {

    @Test
    void add() {
        NotificationTemplateWorkflowTaskType.add("key", "value");
        assertThat(NotificationTemplateWorkflowTaskType.getDescription("key")).isEqualTo("value");
    }

    @Test
    void getDescription_throws_error() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> NotificationTemplateWorkflowTaskType.getDescription("key1"));

        assertThat(exception.getMessage()).isEqualTo("Request type key1 cannot be mapped to notification template workflow task type");
    }

}