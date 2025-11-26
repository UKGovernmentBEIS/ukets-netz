package uk.gov.netz.api.workflow.request.flow.common.taskhandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskCreateService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;

import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUserTaskCreatedHandlerTest {

	@InjectMocks
	private DefaultUserTaskCreatedHandler cut;

	@Mock
	private RequestTaskCreateService requestTaskCreateService;
	
	@Mock
	private RequestTaskTypeRepository requestTaskTypeRepository;

	@Test
	void createRequestTask_dynamic_default_request_type() {
		String requestId = "1";
		String processTaskId = "proc";
		DynamicUserTaskDefinitionKey taskDefinitionKey = DynamicUserTaskDefinitionKey.APPLICATION_REVIEW;
		Map<String, Object> variables = Map.of(
				BpmnProcessConstants.REQUEST_TYPE, "DUMMY_REQUEST_TYPE"
		);
		
		RequestTaskType requestTaskType = RequestTaskType.builder()
				.code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW")
				.expirationKey(null)
				.build();
		
		when(requestTaskTypeRepository.findByCode("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW"))
				.thenReturn(Optional.of(requestTaskType));

		cut.createRequestTask(requestId, processTaskId, taskDefinitionKey.name(), variables);

		verify(requestTaskTypeRepository, times(1)).findByCode("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW");
		verify(requestTaskCreateService, times(1)).create(requestId, processTaskId, requestTaskType);
	}

	@Test
	void createRequestTask_fixed_request_task_type() {
		String requestId = "1";
		String processTaskId = "proc";
		String taskDefinitionKey = "DUMMY_REQUEST_TYPE_APPLICATION_REVIEW";
		Date dueDate = new Date();
		Map<String, Object> variables = Map.of(
				BpmnProcessConstants.REQUEST_TYPE, "DUMMY_REQUEST_TYPE",
				"expKey" + BpmnProcessConstants._EXPIRATION_DATE, dueDate
		);
		
		RequestTaskType requestTaskType = RequestTaskType.builder()
				.code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW")
				.expirationKey("expKey")
				.build();
		
		when(requestTaskTypeRepository.findByCode("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW"))
			.thenReturn(Optional.of(requestTaskType));

		cut.createRequestTask(requestId, processTaskId, taskDefinitionKey, variables);

		verify(requestTaskTypeRepository, times(1)).findByCode("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW");
		verify(requestTaskCreateService, times(1)).create(requestId, processTaskId, requestTaskType, null,
				dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}
}
