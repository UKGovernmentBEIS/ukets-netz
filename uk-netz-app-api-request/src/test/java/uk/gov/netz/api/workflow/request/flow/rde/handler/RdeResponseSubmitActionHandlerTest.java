package uk.gov.netz.api.workflow.request.flow.rde.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class RdeResponseSubmitActionHandlerTest {

    @InjectMocks
    private RdeResponseSubmitActionHandler handler;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private WorkflowService workflowService;

    @Test
    void getTypes() {
        assertThat(handler.getTypes()).containsExactly(RequestTaskActionTypes.RDE_RESPONSE_SUBMIT);
    }
}
