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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, ItemOperatorRepository.class})
class ItemOperatorRepositoryIT extends RequestAbstractTest {

    @Autowired
    private ItemOperatorRepository cut;

    @Test
    void findItems_assigned_to_me() {
        Long account1 = -1L;
        Long account1_2 = -2L;
        String user = "user";
        
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
                Map.of(account1, Set.of(requestTaskTypeCode1),
                        account1_2, Set.of(requestTaskTypeCode2));

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTask requestTask1 =
                createRequestTask(user, request1, requestTaskType1, "t1", request1.getCreationDate());
        createOpenedItem(requestTask1.getId(), user);

        Request request3 = createRequest(account1_2, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        createRequestTask("anotherUser", request3, requestTaskType2, "t3", request3.getCreationDate());

        Request request6 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 6L, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request6, requestTaskType1, "t6", request6.getCreationDate());

        Request request7 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 7L, requestType1, "procInstId4", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request7, requestTaskType2, "t7", request7.getCreationDate());

        Request request8 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 8L, requestType1, "procInstId5", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request8, requestTaskType1, "t8", request8.getCreationDate());

        createRequestTask(null, request1, requestTaskType2, "t9", request1.getCreationDate());

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,PagingRequest.builder().pageNumber(0).pageSize(10).build());

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
        Long account1 = -1L;
        Long account1_2 = -2L;

        String user = "user";
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
                Map.of(account1, Set.of(requestTaskTypeCode1),
                        account1_2, Set.of(requestTaskTypeCode2));
        
        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);

        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request1, requestTaskType2, "t1", request1.getCreationDate());

        Request request3 = createRequest(account1_2, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        RequestTask requestTask3 = createRequestTask("anotherUser", request3, requestTaskType2, "t3", request3.getCreationDate());

        Request request4 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 4L, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        createRequestTask("anotherUser", request4, requestTaskType2, "t4", request4.getCreationDate());

        Request request5 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 5L, requestType1, "procInstId4", statusInProgress, LocalDateTime.now());
        createRequestTask("anotherUser", request5, requestTaskType2, "t5", request5.getCreationDate());

        Request request6 = createRequest(account1_2, CompetentAuthorityEnum.ENGLAND, 6L, requestType1, "procInstId5", statusInProgress, LocalDateTime.now());
        createRequestTask("anotherUser", request6, requestTaskType1, "t6", request5.getCreationDate());

        createRequestTask(null, request1, requestTaskType1, "t7", request1.getCreationDate());

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.OTHERS, scopedRequestTaskTypes,PagingRequest.builder().pageNumber(0).pageSize(10).build());

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());


        Item item = itemPage.getItems().get(0);
        assertThat(item.getRequestId()).isEqualTo(request3.getId());
        assertEquals(item.getCreationDate().truncatedTo(ChronoUnit.MILLIS),
                requestTask3.getStartDate().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(item.getRequestId(), request3.getId());
        assertEquals(item.getRequestType(), request3.getType());
        assertEquals(item.getTaskId(), requestTask3.getId());
        assertEquals(item.getTaskType(), requestTask3.getType());
        assertEquals(item.getTaskAssigneeId(), requestTask3.getAssignee());
        assertEquals(item.getTaskDueDate(), requestTask3.getDueDate());
    }

    @Test
    void findItems_unassigned() {
        Long account1 = -1L;
        Long account1_2 = -2L;

        String user = "user";
        
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
                Map.of(account1, Set.of(requestTaskTypeCode1),
                        account1_2, Set.of(requestTaskTypeCode2));
        
        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);

        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTask requestTask1 = createRequestTask(null, request1, requestTaskType1, "t1", request1.getCreationDate());
        createRequestTask(user, request1, requestTaskType2, "t2", request1.getCreationDate());

        Request request2 = createRequest(account1_2, CompetentAuthorityEnum.ENGLAND, 2L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());
        createRequestTask("anotherUser", request2, requestTaskType1, "t3", request2.getCreationDate());

        Request request3 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request3, requestTaskType2, "t4", request3.getCreationDate());

        Request request4 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 4L, requestType1, "procInstId4", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request4, requestTaskType1, "t5", request4.getCreationDate());

        Request request5 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 5L, requestType1, "procInstId5", statusInProgress, LocalDateTime.now());
        createRequestTask(user, request5, requestTaskType2, "t6", request5.getCreationDate());

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes,PagingRequest.builder().pageNumber(0).pageSize(10).build());

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

    @Test
    void findItems_assigned_to_me_large_account_map() {
        Long account1  = 1L;
        String user = "user";
        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
        
        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        
        String statusInProgress = "inprogress";

        Map<Long, Set<String>> accountScopedMap = new HashMap<>();

        for (int i = 1; i < 1000; i++) {
            accountScopedMap.put(Long.valueOf(i), Set.of(requestTaskTypeCode1));
        }

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTask requestTask1 =
                createRequestTask(user, request1, requestTaskType1, "t1", request1.getCreationDate());
        createOpenedItem(requestTask1.getId(), user);


        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, accountScopedMap, PagingRequest.builder().pageNumber(0).pageSize(10).build());

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

}