package uk.gov.netz.api.workflow.bpmn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class WorkflowTypeProviderIT extends RequestAbstractTest {

    @Autowired
    private WorkflowTypeProvider cut;
    
    @Test
    void findWorkflowEngineByProcessTaskId() {
    	final String assignee = "assignee";
		
		RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE1", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		Request request1 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.CAMUNDA);
		RequestTaskType requestTaskType1 = createRequestTaskType("REQUEST_TASK_TYPE1", requestType1, false, "key1", false, false);
		createRequestTask(assignee, request1, requestTaskType1, "t1", LocalDateTime.now());
		
		RequestType requestType2 = createRequestType("DUMMY_REQUEST_TYPE2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		Request request2 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId2", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.FLOWABLE);
		RequestTaskType requestTaskType2 = createRequestTaskType("REQUEST_TASK_TYPE2", requestType2, false, "key2", false, false);
		createRequestTask(assignee, request2, requestTaskType2, "t2", LocalDateTime.now());

		flushAndClear();
    	
    	//invoke
		WorkflowEngineType actualResult = cut.findWorkflowEngineByProcessTaskId("t1");
    	
    	//assert
    	assertThat(actualResult).isEqualTo(WorkflowEngineType.CAMUNDA);
	}
    
    @Test
    void findWorkflowEngineByRequestId() {
		RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE1", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.CAMUNDA);
		
		RequestType requestType2 = createRequestType("DUMMY_REQUEST_TYPE2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		Request request2 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId2", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.FLOWABLE);

		flushAndClear();
    	
    	//invoke
		WorkflowEngineType actualResult = cut.findWorkflowEngineByRequestId(request2.getId());
    	
    	//assert
    	assertThat(actualResult).isEqualTo(WorkflowEngineType.FLOWABLE);
	}
    
    @Test
    void findWorkflowEngineByProcessInstanceId() {
		RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE1", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.CAMUNDA);
		
		RequestType requestType2 = createRequestType("DUMMY_REQUEST_TYPE2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId2", "IN_PROGRESS", LocalDateTime.now(), WorkflowEngineType.FLOWABLE);

		flushAndClear();
    	
    	//invoke
		WorkflowEngineType actualResult = cut.findWorkflowEngineByProcessInstanceId("procInstId2");
    	
    	//assert
    	assertThat(actualResult).isEqualTo(WorkflowEngineType.FLOWABLE);
	}

}