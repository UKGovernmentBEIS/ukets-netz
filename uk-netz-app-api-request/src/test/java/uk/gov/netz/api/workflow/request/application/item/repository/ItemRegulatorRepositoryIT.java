package uk.gov.netz.api.workflow.request.application.item.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, ItemRegulatorRepository.class})
class ItemRegulatorRepositoryIT extends RequestAbstractTest {

    @Autowired
    private ItemRegulatorRepository cut;

    @Test
    void findItems_assigned_to_me() {
        Long account = 1L;
        String user = "reg";
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        
        String statusInProgress = "inprogress";

        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
                Map.of(CompetentAuthorityEnum.ENGLAND, Set.of(requestTaskTypeCode1));
        
        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);

        Request request1 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTask requestTask1 = createRequestTask(user, request1, requestTaskType1, "t1", request1.getCreationDate());
        createOpenedItem(requestTask1.getId(), user);

        Request request3 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        createRequestTask("another user", request3, requestTaskType1, "t3", request3.getCreationDate());

        Request request4 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        createRequestTask("another user", request4, requestTaskType1, "t4", request4.getCreationDate());

        createRequestTask(null, request1, requestTaskType1, "t5", request1.getCreationDate());

        ItemPage itemPage =
                cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        Item item1 = itemPage.getItems().get(0);
        assertThat(item1.getRequestId()).isEqualTo(request1.getId());
        assertEquals(item1.getCreationDate().truncatedTo(ChronoUnit.MILLIS),
                requestTask1.getStartDate().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(item1.getRequestId(), request1.getId());
        assertEquals(item1.getRequestType(), request1.getType());
        assertEquals(item1.getTaskId(), requestTask1.getId());
        assertEquals(item1.getTaskType(), requestTask1.getType());
        assertEquals(item1.getTaskAssigneeId(), requestTask1.getAssignee());
        assertEquals(item1.getTaskDueDate(), requestTask1.getDueDate());
        assertFalse(item1.isNew());
    }

    @Test
    void findItems_assigned_to_others() {
        Long account = 1L;
        String user = "reg";
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";

        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
                Map.of(CompetentAuthorityEnum.ENGLAND, Set.of(requestTaskTypeCode1));
        
        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);

        Request request1 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request1, requestTaskType1, "t1", request1.getCreationDate());

        Request request3 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request3, requestTaskType2, "t3", request3.getCreationDate());

        Request request4 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        RequestTask requestTask4 = createRequestTask("another user", request4, requestTaskType1, "t4", request4.getCreationDate());

        Request request5 = createRequest(account, CompetentAuthorityEnum.SCOTLAND, null, requestType1, "procInstId4", statusInProgress, LocalDateTime.now());
        createRequestTask("another user", request5, requestTaskType1, "t5", request5.getCreationDate());

        createRequestTask(null, request1, requestTaskType2, "t6", request1.getCreationDate());

        ItemPage itemPage =
                cut.findItems(user, ItemAssignmentType.OTHERS, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        Item item = itemPage.getItems().get(0);
        assertThat(item.getRequestId()).isEqualTo(request4.getId());
        assertEquals(item.getCreationDate().truncatedTo(ChronoUnit.MILLIS),
                requestTask4.getStartDate().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(item.getRequestId(), request4.getId());
        assertEquals(item.getRequestType(), request4.getType());
        assertEquals(item.getTaskId(), requestTask4.getId());
        assertEquals(item.getTaskType(), requestTask4.getType());
        assertEquals(item.getTaskAssigneeId(), requestTask4.getAssignee());
        assertEquals(item.getTaskDueDate(), requestTask4.getDueDate());
    }

    @Test
    void findItems_unassigned() {
        Long account = 1L;
        String user = "reg";
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";

        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
                Map.of(CompetentAuthorityEnum.ENGLAND, Set.of(requestTaskTypeCode1));
        
        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);

        Request request1 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTask requestTask1 = createRequestTask(null, request1, requestTaskType1, "t1", request1.getCreationDate());

        createRequestTask(user, request1, requestTaskType1, "t2", request1.getCreationDate());

        Request request3 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request3, requestTaskType2, "t4", request3.getCreationDate());

        Request request4 = createRequest(account, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        createRequestTask("another user", request4, requestTaskType1, "t5", request4.getCreationDate());

        ItemPage itemPage =
                cut.findItems(user, ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        Item item1 = itemPage.getItems().get(0);
        assertThat(item1.getRequestId()).isEqualTo(request1.getId());
        assertEquals(item1.getCreationDate().truncatedTo(ChronoUnit.MILLIS),
                requestTask1.getStartDate().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(item1.getRequestId(), request1.getId());
        assertEquals(item1.getRequestType(), request1.getType());
        assertEquals(item1.getTaskId(), requestTask1.getId());
        assertEquals(item1.getTaskType(), requestTask1.getType());
        assertEquals(item1.getTaskAssigneeId(), requestTask1.getAssignee());
        assertEquals(item1.getTaskDueDate(), requestTask1.getDueDate());
    }

}