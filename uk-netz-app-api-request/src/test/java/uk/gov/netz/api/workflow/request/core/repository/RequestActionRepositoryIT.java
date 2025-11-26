package uk.gov.netz.api.workflow.request.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestActionRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestActionRepository requestActionRepository;
    
    @Test
    void findAllByRequestId() {
    	RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "histCat1", false, false, false, false, ResourceType.ACCOUNT);
    	
    	Request request1 = createRequest(1L, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
        RequestAction requestAction1 = createRequestAction(request1, "REQUEST_TERMINATED", "userId", "username", LocalDateTime.now());

        Request request2 = createRequest(2L, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
        createRequestAction(request2, "REQUEST_TERMINATED", "userId", "username", LocalDateTime.now());

        List<RequestAction> actual = requestActionRepository.findAllByRequestId(request1.getId());

        assertThat(actual)
                .hasSize(1)
                .containsExactly(requestAction1);
    }

    @Test
    void testLazyInitialization_whenLazyBasicAccessedAfterSessionCloses_thenThrowException() {
    	RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "histCat1", false, false, false, false, ResourceType.ACCOUNT);
    	Request request1 = createRequest(1L, CompetentAuthorityEnum.ENGLAND, 1L, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequestAction(request1, "RFI_SUBMITTED", "userId", "username", LocalDateTime.now());
        
    	flushAndClear();

        final List<RequestAction> requestActions = requestActionRepository.findAll();

        TestTransaction.end();

        assertEquals(1, requestActions.size());
        assertThrows(LazyInitializationException.class, () -> requestActions.get(0).getPayload());
    }
}
