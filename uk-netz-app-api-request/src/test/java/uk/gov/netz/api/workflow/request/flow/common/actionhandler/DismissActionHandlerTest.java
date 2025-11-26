package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestTaskPayload;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants.REQUEST_ID;

@ExtendWith(MockitoExtension.class)
class DismissActionHandlerTest {

    @InjectMocks
    private DismissActionHandler handler;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private RequestTaskService requestTaskService;

    @Test
    void process() {
        AppUser appUser = AppUser.builder().userId("user").build();

        SystemMessageNotificationRequestTaskPayload expectedRequestTaskPayload =
            SystemMessageNotificationRequestTaskPayload.builder().build();

        RequestTask requestTask = RequestTask.builder()
            .id(1L)
            .payload(SystemMessageNotificationRequestTaskPayload.builder().build())
            .request(Request.builder()
                .id("10")
                .processInstanceId("abc")
                .build())
            .build();

        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);

        //invoke
        RequestTaskPayload actualRequestTaskPayload = handler.process(requestTask.getId(),
            RequestTaskActionTypes.SYSTEM_MESSAGE_DISMISS, appUser, new RequestTaskActionEmptyPayload());

        assertEquals(expectedRequestTaskPayload, actualRequestTaskPayload);

        //verify
        verify(workflowService, times(1))
            .completeTask(requestTask.getProcessTaskId(), Map.of(REQUEST_ID, requestTask.getRequest().getId()));
    }

    @Test
    void getTypes() {
        assertThat(handler.getTypes()).containsOnly(RequestTaskActionTypes.SYSTEM_MESSAGE_DISMISS);
    }
}
