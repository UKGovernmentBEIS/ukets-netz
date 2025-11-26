package uk.gov.netz.api.workflow.request.application.taskview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserDTO;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTaskMapperTest {
	
	private RequestTaskMapper mapper;
	
	@BeforeEach
	public void init() {
		mapper = Mappers.getMapper(RequestTaskMapper.class);
	}
	
	@Test
	void toTaskDTO_no_assignee_user_assignable() {
	    Long requestTaskId = 2L;
		RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskTypeCode").assignable(true).build();
		
		RequestTask requestTask = RequestTask.builder().id(requestTaskId).type(requestTaskType).build();
		
		//invoke
		RequestTaskDTO result = mapper.toTaskDTO(requestTask, null);
		
		//assert
		assertThat(result.getAssigneeFullName()).isNull();
		assertThat(result.getAssigneeUserId()).isNull();
		assertThat(result.getType()).isEqualTo("requestTaskTypeCode");
		assertThat(result.getId()).isEqualTo(requestTaskId);
		assertThat(result.isAssignable()).isTrue();
    }
	
	@Test
	void toTaskDTO_with_assignee_user() {
        Long requestTaskId = 2L;
        String task_assignee = "task_assignee";
        
        RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskTypeCode").build();
		RequestTask requestTask = RequestTask.builder().id(requestTaskId).type(requestTaskType).assignee(task_assignee).build();
        
		final String fn = "fn";
		final String ln = "ln";
		UserDTO assigneeUser = OperatorUserDTO.builder()
							.firstName(fn)
							.lastName(ln)
							.build();
		
		//invoke
		RequestTaskDTO result = mapper.toTaskDTO(requestTask, assigneeUser);
		
		//assert
		assertThat(result.getAssigneeFullName()).isEqualTo(fn + " " + ln);
		assertThat(result.getAssigneeUserId()).isEqualTo(task_assignee);
		assertThat(result.getType()).isEqualTo("requestTaskTypeCode");
		assertThat(result.getId()).isEqualTo(requestTaskId);
		assertThat(result.isAssignable()).isFalse();
	}

}
