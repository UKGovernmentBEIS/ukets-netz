package uk.gov.netz.api.workflow.request.application.item.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
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

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, ItemByRequestRegulatorRepository.class})
class ItemByRequestRegulatorRepositoryIT extends RequestAbstractTest {

    @Autowired
    private ItemByRequestRegulatorRepository cut;

    @Test
    void findItemsByRequestId() {
    	String requestTypeCode1 = "DUMMY_REQUEST_TYPE";
    	
    	String requestTaskTypeCode1 = "DUMMY_REQUEST_TASK_TYPE_APPLICATION_REVIEW";
        String requestTaskTypeCode2 = "DUMMY_REQUEST_TASK_TYPE2";
        
        String statusInProgress = "inprogress";
    	
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
                Map.of(CompetentAuthorityEnum.ENGLAND, Set.of(requestTaskTypeCode1, requestTaskTypeCode2));

        RequestType requestType1 = createRequestType(requestTypeCode1, "descr", "processdef", "histCat", false, false, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", statusInProgress, LocalDateTime.now());
        RequestTaskType requestTaskType1 = createRequestTaskType(requestTaskTypeCode1, requestType1, false, "key1", false, false);
        RequestTask requestTask1 = createRequestTask("oper1", request1, requestTaskType1, "t1", LocalDateTime.now());
        RequestTaskType requestTaskType2 = createRequestTaskType(requestTaskTypeCode2, requestType1, false, "key2", false, false);
        RequestTask requestTask2 = createRequestTask("oper1", request1, requestTaskType2, "t2", LocalDateTime.now());

        Request request2 = createRequest(2L, CompetentAuthorityEnum.SCOTLAND, null, requestType1, "procInstId2", statusInProgress, LocalDateTime.now().plusDays(1)); 
        createRequestTask("oper1", request2, requestTaskType1, "t4", LocalDateTime.now());

        ItemPage itemPage = cut.findItemsByRequestId(scopedRequestTaskTypes, request1.getId());

        assertEquals(2L, itemPage.getTotalItems());
        assertEquals(2, itemPage.getItems().size());


        Item item2 = itemPage.getItems().get(0);
        assertThat(item2.getRequestId()).isEqualTo(request1.getId());
        assertEquals(item2.getCreationDate().truncatedTo(ChronoUnit.MILLIS),
                requestTask2.getStartDate().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(item2.getRequestId(), request1.getId());
        assertEquals(item2.getRequestType(), request1.getType());
        assertEquals(item2.getTaskId(), requestTask2.getId());
        assertEquals(item2.getTaskType(), requestTask2.getType());
        assertEquals(item2.getTaskAssigneeId(), requestTask2.getAssignee());
        assertEquals(item2.getTaskDueDate(), requestTask2.getDueDate());

        Item item1 = itemPage.getItems().get(1);
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