package uk.gov.netz.api.workflow.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestCreateService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestIdGenerator;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestIdGeneratorResolver;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartProcessRequestServiceTest {

    @InjectMocks
    private StartProcessRequestService startProcessRequestService;

    @Mock
    private RequestCreateService requestCreateService;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private RequestIdGeneratorResolver requestIdGeneratorResolver;

    @Test
    void startProcess() {
        final String requestId = "1";
        final RequestType requestType = RequestType.builder().code("requestType").build();
        final RequestParams params = RequestParams.builder()
                .type(requestType.getCode())
                .processVars(Map.of(
                		"proccVar1", "processVar1Val"
                		))
                .build();
        final String processInstanceId = "prInstanceId";
        
        RequestIdGenerator requestIdGenerator = Mockito.mock(RequestIdGenerator.class);
        when(requestIdGeneratorResolver.get(requestType.getCode())).thenReturn(requestIdGenerator);
        when(requestIdGenerator.generate(params)).thenReturn(requestId);
        
        Request request = Request.builder().id(requestId).type(requestType).build();

        when(requestCreateService.createRequest(params.withRequestId(requestId), "IN_PROGRESS"))
            .thenReturn(request);

        Map<String, Object> processVars = new HashMap<>();
        processVars.put(BpmnProcessConstants.REQUEST_ID, requestId);
        processVars.put(BpmnProcessConstants.REQUEST_TYPE, requestType.getCode());
        processVars.putAll(params.getProcessVars());
        
        when(workflowService.startProcessDefinition(request, processVars))
            .thenReturn(processInstanceId);

        // Invoke
        Request result = startProcessRequestService.startProcess(params);

        //assert
        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getProcessInstanceId()).isEqualTo(processInstanceId);
        // Verify
        verify(requestIdGeneratorResolver, times(1)).get(requestType.getCode());
        verify(requestIdGenerator, times(1)).generate(params);
        verify(requestCreateService, times(1)).createRequest(params.withRequestId(requestId), "IN_PROGRESS");
        verify(workflowService, times(1)).startProcessDefinition(request, processVars);
        assertThat(result.getProcessInstanceId()).isEqualTo(processInstanceId);
    }

    @Test
    void reStartProcess() {
        final String requestId = "1";
        final RequestType requestType = RequestType.builder().code("requestType").build();
        final String processInstanceId = "prInstanceId";

        Request request = Request.builder()
                .id(requestId)
                .type(requestType)
                .processInstanceId("previousInstanceId")
                .status("COMPLETED")
                .build();

        Map<String, Object> processVars = new HashMap<>();
        processVars.put(BpmnProcessConstants.REQUEST_ID, request.getId());
        processVars.put(BpmnProcessConstants.REQUEST_TYPE, request.getType().getCode());

        when(workflowService.reStartProcessDefinition(request, processVars))
                .thenReturn(processInstanceId);

        // Invoke
        startProcessRequestService.reStartProcess(request);

        // Verify
        verify(workflowService, times(1)).reStartProcessDefinition(request, processVars);
        assertThat(request.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(request.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void reStartProcess_with_process_vars() {
        final String requestId = "1";
        final RequestType requestType = RequestType.builder().code("requestType").build();
        final String processInstanceId = "prInstanceId";

        Request request = Request.builder()
                .id(requestId)
                .type(requestType)
                .processInstanceId("previousInstanceId")
                .status("COMPLETED")
                .build();

        Map<String, Object> processVars = new HashMap<>();
        processVars.put(BpmnProcessConstants.REQUEST_ID, request.getId());
        processVars.put(BpmnProcessConstants.REQUEST_TYPE, request.getType().getCode());
        processVars.put(BpmnProcessConstants.PAYMENT_EXPIRATION_DATE, LocalDate.of(2025, 6,5));

        when(workflowService.reStartProcessDefinition(request, processVars))
                .thenReturn(processInstanceId);

        // Invoke
        startProcessRequestService.reStartProcess(request, processVars);

        // Verify
        verify(workflowService, times(1)).reStartProcessDefinition(request, processVars);
        assertThat(request.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(request.getStatus()).isEqualTo("IN_PROGRESS");
    }
}
