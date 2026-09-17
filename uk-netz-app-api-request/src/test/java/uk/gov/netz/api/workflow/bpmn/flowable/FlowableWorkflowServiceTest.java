package uk.gov.netz.api.workflow.bpmn.flowable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlowableWorkflowServiceTest {
	
	@InjectMocks
    private FlowableWorkflowService workflowService;
	
	@Mock
	private RuntimeService runtimeService;
	
	@Mock
    private TaskService taskService;
	
	@Mock
    private ExecutionQuery executionQuery;
	
	@Test
	void completeTask() {
		final String processTaskId = "1";
		final Map<String, Object> vars = Map.of("test1", "val1");
		
		//invoke
		workflowService.completeTask(processTaskId, vars);
	    
	    //verify
	    verify(taskService, times(1)).complete(processTaskId, vars);
	}
	
	@Test
	void getProcessInstanceIdByBusinessKey() {
		final String businessKey = "1";
		
		ProcessInstanceQuery eqMock1 = Mockito.mock(ProcessInstanceQuery.class);
		ProcessInstanceQuery eqMock2 = Mockito.mock(ProcessInstanceQuery.class);
		ProcessInstance processInstanceMock = Mockito.mock(ProcessInstance.class);
		
		when(runtimeService.createProcessInstanceQuery()).thenReturn(eqMock1);
		when(eqMock1.processInstanceBusinessKey(businessKey)).thenReturn(eqMock2);
		when(eqMock2.singleResult()).thenReturn(processInstanceMock);
		when(processInstanceMock.getProcessInstanceId()).thenReturn("ExpectedId");
		
		//invoke
		String result = workflowService.getProcessInstanceIdByBusinessKey(businessKey);
		
		assertThat(result).isEqualTo("ExpectedId");
		
		verify(runtimeService, times(1)).createProcessInstanceQuery();
		verify(eqMock1, times(1)).processInstanceBusinessKey(businessKey);
		verify(eqMock2, times(1)).singleResult();
		verify(processInstanceMock, times(1)).getProcessInstanceId();
	    
	}
	
	@Test
    void shouldSendMessageToAllMatchingExecutions() {
		when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
		when(executionQuery.messageEventSubscriptionName(any())).thenReturn(executionQuery);
		when(executionQuery.processInstanceBusinessKey(anyString(), Mockito.eq(true))).thenReturn(executionQuery);

        Execution e1 = Mockito.mock(Execution.class);
        Execution e2 = Mockito.mock(Execution.class);

        when(e1.getId()).thenReturn("exec1");
        when(e2.getId()).thenReturn("exec2");

        when(executionQuery.list()).thenReturn(List.of(e1, e2));

        when(runtimeService.getVariable("exec1", "myVar")).thenReturn("VALUE");
        when(runtimeService.getVariable("exec2", "myVar")).thenReturn("VALUE");

        workflowService.sendEventInProcessesThatContainVariable(
                "123",
                "MESSAGE",
                "myVar",
                "VALUE");

        verify(runtimeService).messageEventReceived("MESSAGE", "exec1", Map.of());
        verify(runtimeService).messageEventReceived("MESSAGE", "exec2", Map.of());
    }

}
