package uk.gov.netz.api.workflow.bpmn.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.CustomUserTaskCreatedHandler;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.DefaultUserTaskCreatedHandler;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.DynamicUserTaskDefinitionKey;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTaskCreatedListenerTest {

	@InjectMocks
	private UserTaskCreatedListener cut;
	
	@Spy
    private ArrayList<CustomUserTaskCreatedHandler> customUserTaskCreatedHandler;
	
	@Mock
	private DefaultUserTaskCreatedHandler defaultUserTaskCreatedHandler;
	
	@Mock
	private TestCustomUserTaskCreatedHandler testCustomUserTaskCreatedHandler;
	
	@Mock
	private DelegateTask taskDelegate;
	
	@BeforeEach
    void setUp() {
		customUserTaskCreatedHandler.add(testCustomUserTaskCreatedHandler);
    }

	@Test
	void onTaskCreatedEvent_default_handler() {
		final String requestId = "1";
		final String processTaskId ="taskid";
		final String taskDefinitionKey = DynamicUserTaskDefinitionKey.APPLICATION_REVIEW.name();
		final Map<String, Object> variables = Map.of("var1", "val1");
		
		when(taskDelegate.getVariable(BpmnProcessConstants.REQUEST_ID)).thenReturn(requestId);
		when(taskDelegate.getId()).thenReturn(processTaskId);
		when(taskDelegate.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
		when(taskDelegate.getVariables()).thenReturn(variables);
		
		// Invoke
		cut.onTaskCreatedEvent(taskDelegate);

		// Verify
		verify(taskDelegate, times(1)).getVariable(BpmnProcessConstants.REQUEST_ID);
		verify(taskDelegate, times(1)).getId();
		verify(taskDelegate, times(1)).getTaskDefinitionKey();
		verify(taskDelegate, times(1)).getVariables();
		verify(testCustomUserTaskCreatedHandler, times(1)).getTaskDefinitionKey();
		verify(defaultUserTaskCreatedHandler, times(1)).createRequestTask(
				requestId, processTaskId, taskDefinitionKey, variables);
	}
	
	@Test
	void onTaskCreatedEvent_custom_handler() {
		final String requestId = "1";
		final String processTaskId ="taskid";
		final String taskDefinitionKey = "taskDefinitionKey";
		final Map<String, Object> variables = Map.of("var1", "val1");
		
		when(taskDelegate.getVariable(BpmnProcessConstants.REQUEST_ID)).thenReturn(requestId);
		when(taskDelegate.getId()).thenReturn(processTaskId);
		when(taskDelegate.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
		when(taskDelegate.getVariables()).thenReturn(variables);
		
		when(testCustomUserTaskCreatedHandler.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
		
		// Invoke
		cut.onTaskCreatedEvent(taskDelegate);

		// Verify
		verify(taskDelegate, times(1)).getVariable(BpmnProcessConstants.REQUEST_ID);
		verify(taskDelegate, times(1)).getId();
		verify(taskDelegate, times(1)).getTaskDefinitionKey();
		verify(taskDelegate, times(1)).getVariables();
		verify(testCustomUserTaskCreatedHandler, times(1)).getTaskDefinitionKey();
		
		verify(testCustomUserTaskCreatedHandler, times(1))
			.createRequestTask(requestId, processTaskId, taskDefinitionKey, variables);
		
		verifyNoInteractions(defaultUserTaskCreatedHandler);
	}


	private static class TestCustomUserTaskCreatedHandler implements CustomUserTaskCreatedHandler {

		@Override
		public String getTaskDefinitionKey() {
			return null;
		}

		@Override
		public void createRequestTask(String requestId, String processTaskId, String taskDefinitionKey, Map<String, Object> variables) {

		}
	}
}
