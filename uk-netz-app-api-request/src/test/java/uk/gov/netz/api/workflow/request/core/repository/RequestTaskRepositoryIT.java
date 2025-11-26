package uk.gov.netz.api.workflow.request.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestTaskRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestTaskRepository repository;
    
    @Test
    void findByRequestTypeAndAssignee() {
    	final String assignee = "assignee";
		final String anotherAsignee = "another_assignee";
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request requestInstallationAccountOpening = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
        RequestTask requestTask1 = createRequestTask(assignee, requestInstallationAccountOpening, requestTaskType, "t1", LocalDateTime.now());
        Request requestInstallationAccountOpeningAnotherAssignee = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(anotherAsignee, requestInstallationAccountOpeningAnotherAssignee, requestTaskType, "t2", LocalDateTime.now());
        Request requestPermitIssuance = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
        RequestTask requestTask2 = createRequestTask(assignee, requestPermitIssuance, requestTaskType, "t3", LocalDateTime.now());
        
		flushAndClear();
    	
    	//invoke
    	List<RequestTask> tasksFound = repository.findByRequestTypeAndAssignee(requestType, assignee);
    	
    	//assert
    	assertThat(tasksFound).hasSize(2);
    	assertThat(tasksFound.get(0).getId()).isEqualTo(requestTask1.getId());
		assertThat(tasksFound.get(1).getId()).isEqualTo(requestTask2.getId());
	}

	@Test
	void findByRequestTypeAndAssigneeAndRequestAccountId() {
		final String assignee = "assignee";
		final Long accountId = 1L;
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request request1 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask requestTask1 = createRequestTask(assignee, request1, requestTaskType, "t1", LocalDateTime.now());
		
		Request request2 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
		RequestTask requestTask2 = createRequestTask(assignee, request2, requestTaskType, "t2", LocalDateTime.now());
		
		Request request3 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(assignee, request3, requestTaskType, "t3", LocalDateTime.now());
		
		Request request4 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId4", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask("other_assignee", request4, requestTaskType, "t4", LocalDateTime.now());

		flushAndClear();

		//invoke
		List<RequestTask> tasksFound = repository.findByRequestTypeAndAssigneeAndRequestAccountId(requestType, assignee, accountId);

		//assert
		assertThat(tasksFound).hasSize(2);
		assertThat(tasksFound).extracting(RequestTask::getId).containsExactly(requestTask1.getId(), requestTask2.getId());
	}
	
	@Test
    void findByAssignee() {
        final String assignee = "assignee";
		final String anotherAsignee = "another_assignee";
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request request1 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask requestTask1 = createRequestTask(assignee, request1, requestTaskType, "t1", LocalDateTime.now());
		
		Request request2 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(anotherAsignee, request2, requestTaskType, "t3", LocalDateTime.now());
		
		flushAndClear();
        
        //invoke
        List<RequestTask> tasksFound = repository.findByAssignee(assignee);
        
        //assert
        assertThat(tasksFound).hasSize(1);
        assertThat(tasksFound.get(0).getId()).isEqualTo(requestTask1.getId());
    }

    @Test
    void findByAssigneeAndRequestAccountId() {
        String assignee1 = "assignee1";
		String assignee2 = "assignee2";

		Long accountId1 = 1L;
		Long accountId2 = 2L;
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request request1 = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask request1Task1 = createRequestTask(assignee1, request1, requestTaskType, "t1", LocalDateTime.now());
		
		Request request2 = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(assignee2, request2, requestTaskType, "t2", LocalDateTime.now());
		
		Request request3 = createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, null, requestType,"procInstId3",  "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(assignee1, request3, requestTaskType, "t3", LocalDateTime.now());
		
		
		flushAndClear();

		List<RequestTask> requestTasksFound =
            repository.findByAssigneeAndRequestAccountId(assignee1, accountId1);

        assertThat(requestTasksFound).containsOnly(request1Task1);
    }

	@Test
	void findByUnassignedAndRequestAccountId() {
		String assignee1 = "assignee1";

		Long accountId1 = 1L;

		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request request1 = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask request1Task1 = createRequestTask(null, request1, requestTaskType, "t1", LocalDateTime.now());

		Request request3 = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(assignee1, request3, requestTaskType, "t3", LocalDateTime.now());

		flushAndClear();

		List<RequestTask> requestTasksFound =
			repository.findByUnassignedAndRequestAccountId(accountId1);

		assertThat(requestTasksFound).containsOnly(request1Task1);
	}

    @Test
    void findByRequestId() {
        String user1 = "user1";
        String user2 = "user2";
        
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);

		Request request1 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask request1Task1 = createRequestTask(user1, request1, requestTaskType, "t1", LocalDateTime.now());
		RequestTask request1Task2 = createRequestTask(user2, request1, requestTaskType, "t2", LocalDateTime.now());
		
		Request request2 = createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(user1, request2, requestTaskType, "t3", LocalDateTime.now());

        flushAndClear();

        List<RequestTask> requestTasksFound = repository.findByRequestId(request1.getId());

        assertThat(requestTasksFound).containsExactlyInAnyOrder(request1Task1, request1Task2);
    }
    
    @Test
    void findByTypeInAndRequestAccountId() {
        String assignee = "assignee";

		Long accountId1 = 1L;
		Long accountId2 = 2L;
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION", requestType, false, "key1", false, false);
		RequestTaskType requestTaskType2 = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);
		RequestTaskType requestTaskType3 = createRequestTaskType("ANOTHER_TASK_TYPE", requestType, false, "key1", false, false);

		Request request = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
		RequestTask request1Task1 = createRequestTask(assignee, request, requestTaskType, "t1", LocalDateTime.now());
		
		Request requestAnotherTaskType = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "COMPLETED", LocalDateTime.now());
		createRequestTask(assignee, requestAnotherTaskType, requestTaskType3, "t2", LocalDateTime.now());
		
		Request requestAnotherAccountId = createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
		createRequestTask(assignee, requestAnotherAccountId, requestTaskType, "t3", LocalDateTime.now());
		
		
		flushAndClear();

		List<RequestTask> requestTasksFound =
            repository.findByTypeInAndRequestAccountId(Set.of(requestTaskType, requestTaskType2), accountId1);

        assertThat(requestTasksFound).containsOnly(request1Task1);
    }
    
    @Test
    void findAccountIdsByTaskAssigneeAndTaskTypeInAndVerificationBody() {
    	final String assignee = "assignee";
		final String anotherAsignee = "another_assignee";
		final Long vbId = 1L;
		final Long accountId1 = 1L;
		final Long accountId2 = 2L;
		
		RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
		RequestTaskType requestTaskType1 = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION", requestType, false, "key1", false, false);
		RequestTaskType requestTaskType2 = createRequestTaskType("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", requestType, false, "key1", false, false);
		RequestTaskType requestTaskType3 = createRequestTaskType("ANOTHER_TASK_TYPE", requestType, false, "key1", false, false);

		Request requestAccountOpening = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, vbId, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(assignee, requestAccountOpening, requestTaskType1, "t1", LocalDateTime.now());
        Request requestAccountOpeningAnotherAssignee = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, vbId, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(anotherAsignee, requestAccountOpeningAnotherAssignee, requestTaskType1, "t2", LocalDateTime.now());
        Request requestAccountOpeningAnotherVb = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, 2L, requestType, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(assignee, requestAccountOpeningAnotherVb, requestTaskType1, "t3", LocalDateTime.now());
        Request requestAccountOpeningAnotherTaskType = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, vbId, requestType, "procInstId4", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(assignee, requestAccountOpeningAnotherTaskType, requestTaskType3, "t4", LocalDateTime.now());
        Request requestPermitIssuance = createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, vbId, requestType, "procInstId5", "IN_PROGRESS", LocalDateTime.now());
        createRequestTask(assignee, requestPermitIssuance, requestTaskType2, "t5", LocalDateTime.now());
        
		flushAndClear();
    	
    	//invoke
    	List<Long> accountIdsFound = repository.findAccountIdsByTaskAssigneeAndTaskTypeInAndVerificationBody(
    			assignee, Set.of("DUMMY_REQUEST_TYPE_APPLICATION", "DUMMY_REQUEST_TYPE_APPLICATION_REVIEW"), vbId);
    	
    	//assert
    	assertThat(accountIdsFound).hasSize(2).containsExactlyInAnyOrder(accountId1, accountId2);
	}
    
}