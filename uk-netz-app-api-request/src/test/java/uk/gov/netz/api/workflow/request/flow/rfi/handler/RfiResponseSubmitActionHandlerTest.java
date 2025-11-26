package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RfiResponseSubmitActionHandlerTest {

    @InjectMocks
    private RfiResponseSubmitActionHandler handler;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private WorkflowService workflowService;

    @Test
    void getTypes() {
        assertThat(handler.getTypes()).containsExactly(RequestTaskTypes.RFI_RESPONSE_SUBMIT);
    }
}
