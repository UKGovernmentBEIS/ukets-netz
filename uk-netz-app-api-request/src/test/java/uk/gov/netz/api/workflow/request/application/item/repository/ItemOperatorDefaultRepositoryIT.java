package uk.gov.netz.api.workflow.request.application.item.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemOrderBy;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemSearchCriteriaDTO;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
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
@Import({ObjectMapper.class, ItemOperatorDefaultRepository.class})
class ItemOperatorDefaultRepositoryIT extends RequestAbstractTest {

    @Autowired
    private ItemOperatorDefaultRepository cut;

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

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, null));

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

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.OTHERS, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, null));

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

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, null));

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


        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, accountScopedMap,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, null));

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
    void findItems_orderByNewestFirst() {
        Long account1 = -1L;
        String user = "user";

        LocalDateTime t1 = LocalDateTime.of(Year.now().minusYears(2).getValue(), 1, 1, 1, 1);
        LocalDateTime t2 = LocalDateTime.of(Year.now().getValue(), 1, 1, 1, 1);
        LocalDateTime t3 = LocalDateTime.of(Year.now().minusYears(1).getValue(), 1, 1, 1, 1);

        LocalDate d1 = LocalDate.of(Year.now().getValue(), 1, 1);
        LocalDate d2 = LocalDate.of(Year.now().minusYears(2).getValue(), 1, 1);
        LocalDate d3 = LocalDate.of(Year.now().minusYears(1).getValue(), 1, 1);

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
            Map.of(account1, Set.of(requestTaskTypeCode1, requestTaskTypeCode2));


        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        createRequestTask(user, request1, requestTaskType2, "t3", t3, d3);

        createRequestTask(user, request1, requestTaskType2, "t2", t2, d2);

        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        createRequestTask(user, request1, requestTaskType1, "t1", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, null));

        assertEquals(3L, itemPage.getTotalItems());
        assertEquals(3, itemPage.getItems().size());

        assertEquals(t2, itemPage.getItems().getFirst().getCreationDate());
        assertEquals(t3, itemPage.getItems().get(1).getCreationDate());
        assertEquals(t1, itemPage.getItems().get(2).getCreationDate());
    }

    @Test
    void findItems_orderByOldestFirst() {
        Long account1 = -1L;
        String user = "user";

        LocalDateTime t1 = LocalDateTime.of(Year.now().minusYears(2).getValue(), 1, 1, 1, 1);
        LocalDateTime t2 = LocalDateTime.of(Year.now().getValue(), 1, 1, 1, 1);
        LocalDateTime t3 = LocalDateTime.of(Year.now().minusYears(1).getValue(), 1, 1, 1, 1);

        LocalDate d1 = LocalDate.of(Year.now().getValue(), 1, 1);
        LocalDate d2 = LocalDate.of(Year.now().minusYears(2).getValue(), 1, 1);
        LocalDate d3 = LocalDate.of(Year.now().minusYears(1).getValue(), 1, 1);

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
            Map.of(account1, Set.of(requestTaskTypeCode1, requestTaskTypeCode2));


        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        createRequestTask(user, request1, requestTaskType2, "t3", t3, d3);

        createRequestTask(user, request1, requestTaskType2, "t2", t2, d2);

        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        createRequestTask(user, request1, requestTaskType1, "t1", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.OLDEST_FIRST, null, null));

        assertEquals(3L, itemPage.getTotalItems());
        assertEquals(3, itemPage.getItems().size());

        assertEquals(t1, itemPage.getItems().getFirst().getCreationDate());
        assertEquals(t3, itemPage.getItems().get(1).getCreationDate());
        assertEquals(t2, itemPage.getItems().get(2).getCreationDate());
    }

    @Test
    void findItems_orderByNearestDueDate() {
        Long account1 = -1L;
        String user = "user";

        LocalDateTime t1 = LocalDateTime.of(Year.now().plusYears(1).getValue(), 1, 1, 1, 1);
        LocalDateTime t2 = LocalDateTime.of(Year.now().plusYears(2).getValue(), 1, 1, 1, 1);
        LocalDateTime t3 = LocalDateTime.of(Year.now().plusYears(3).getValue(), 1, 1, 1, 1);

        LocalDate d1 = LocalDate.of(Year.now().plusYears(3).getValue(), 1, 1);
        LocalDate d2 = LocalDate.of(Year.now().plusYears(2).getValue(), 1, 1);
        LocalDate d3 = LocalDate.of(Year.now().plusYears(1).getValue(), 1, 1);

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
            Map.of(account1, Set.of(requestTaskTypeCode1, requestTaskTypeCode2));

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        createRequestTask(user, request1, requestTaskType2, "t2", t2, d2);

        createRequestTask(user, request1, requestTaskType2, "t3", t3, d3);

        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        createRequestTask(user, request1, requestTaskType1, "t1", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEAREST_DUE_DATE, null, null));

        assertEquals(3L, itemPage.getTotalItems());
        assertEquals(3, itemPage.getItems().size());

        assertEquals(t3, itemPage.getItems().getFirst().getCreationDate());
        assertEquals(t2, itemPage.getItems().get(1).getCreationDate());
        assertEquals(t1, itemPage.getItems().get(2).getCreationDate());
    }

    @Test
    void findItems_filterByRequestType() {
        Long account1 = -1L;
        String user = "user";

        LocalDateTime t1 = LocalDateTime.now();

        LocalDate d1 = LocalDate.now();

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE_1";
        String requestTypeCode2 = "DUMMY_REQUEST_TYPE_2";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes =
            Map.of(account1, Set.of(requestTaskTypeCode1, requestTaskTypeCode2));


        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());

        RequestType requestType2 = createRequestType(requestTypeCode2, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request2 = createRequest(account1, CompetentAuthorityEnum.ENGLAND, 3L, requestType2, "procInstId2", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        createRequestTask(user, request1, requestTaskType2, "t1", t1, d1);

        createRequestTask(user, request2, requestTaskType2, "t2", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, requestTypeCode1, null));

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        assertEquals(requestTypeCode1, itemPage.getItems().getFirst().getRequestType().getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"account", "Name", "ACCOUNT", "NAME", "account", "name", "accountName", "ccountNam", "ccountnam"})
    void findItems_filterByAccountName(String searchTerm) {
        Long accountId1 = -1L;
        TestAccount account1 = createAccount(accountId1, "accountName", CompetentAuthorityEnum.ENGLAND, "businessId1");
        Long accountId2 = -2L;
        TestAccount account2 = createAccount(accountId2, "other", CompetentAuthorityEnum.ENGLAND, "businessId2");
        Long accountId3 = -3L;
        TestAccount account3 = createAccount(accountId3, "other2", CompetentAuthorityEnum.ENGLAND, "businessId3");
        String user = "user";

        LocalDateTime t1 = LocalDateTime.now();

        LocalDate d1 = LocalDate.now();

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE_1";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(
            accountId1, Set.of(requestTaskTypeCode1),
            accountId2, Set.of(requestTaskTypeCode1)
        );

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());

        Request request2 = createRequest(account2.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());

        Request request3 = createRequest(account3.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key2", false, false);
        RequestTask requestTask1 = createRequestTask(user, request1, requestTaskType1, "t1", t1, d1);

        createRequestTask(user, request2, requestTaskType1, "t2", t1, d1);
        createRequestTask(user, request3, requestTaskType1, "t3", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, searchTerm));

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        assertEquals(requestTask1.getId(), itemPage.getItems().getFirst().getTaskId());
    }

    @Test
    void findItems_filterByAccountBusinessId() {
        Long accountId1 = -1L;
        TestAccount account1 = createAccount(accountId1, "account", CompetentAuthorityEnum.ENGLAND, "B1");
        Long accountId2 = -2L;
        TestAccount account2 = createAccount(accountId2, "account", CompetentAuthorityEnum.ENGLAND, "B2");
        Long accountId3 = -3L;
        TestAccount account3 = createAccount(accountId3, "account", CompetentAuthorityEnum.ENGLAND, "B3");
        String user = "user";

        LocalDateTime t1 = LocalDateTime.now();

        LocalDate d1 = LocalDate.now();

        String requestTypeCode1 = "DUMMY_REQUEST_TYPE_1";

        String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";

        String statusInProgress = "inprogress";

        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(
            accountId1, Set.of(requestTaskTypeCode1),
            accountId2, Set.of(requestTaskTypeCode1)
        );

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(account1.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());

        Request request2 = createRequest(account2.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId2", statusInProgress, LocalDateTime.now());

        Request request3 = createRequest(account3.getId(), CompetentAuthorityEnum.ENGLAND, 3L, requestType1, "procInstId3", statusInProgress, LocalDateTime.now());

        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key2", false, false);
        RequestTask requestTask1 = createRequestTask(user, request1, requestTaskType1, "t1", t1, d1);

        createRequestTask(user, request2, requestTaskType1, "t2", t1, d1);
        createRequestTask(user, request3, requestTaskType1, "t3", t1, d1);

        ItemPage itemPage = cut.findItems(user, ItemAssignmentType.ME, scopedRequestTaskTypes,
            PagingRequest.builder().pageNumber(0).pageSize(10).build(), getItemSearchCriteria(ItemOrderBy.NEWEST_FIRST, null, "B1"));

        assertEquals(1L, itemPage.getTotalItems());
        assertEquals(1, itemPage.getItems().size());

        assertEquals(requestTask1.getId(), itemPage.getItems().getFirst().getTaskId());
    }

    private TestAccount createAccount(Long id, String accountName, CompetentAuthorityEnum ca, String businessId) {
        TestAccount account = TestAccount.builder()
            .id(id)
            .competentAuthority(ca)
            .status(TestAccountStatus.DUMMY)
            .emissionTradingScheme(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME)
            .name(accountName)
            .businessId(businessId)
            .build();

        entityManager.persist(account);

        return account;
    }

    private ItemSearchCriteriaDTO getItemSearchCriteria(ItemOrderBy orderBy,
                                                        String requestType,
                                                        String searchTerm) {
        return ItemSearchCriteriaDTO.builder().orderBy(orderBy).requestType(requestType).searchTerm(searchTerm).build();
    }
}