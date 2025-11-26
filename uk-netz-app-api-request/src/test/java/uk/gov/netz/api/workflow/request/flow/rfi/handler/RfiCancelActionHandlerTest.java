package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

@ExtendWith(MockitoExtension.class)
class RfiCancelActionHandlerTest {

    @InjectMocks
    private RfiCancelActionHandler handler;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private WorkflowService workflowService;


    @Test
    void getTypes() {
        assertThat(handler.getRequestTaskTypes()).containsExactly(RequestTaskTypes.WAIT_FOR_RFI_RESPONSE);
    }
}
