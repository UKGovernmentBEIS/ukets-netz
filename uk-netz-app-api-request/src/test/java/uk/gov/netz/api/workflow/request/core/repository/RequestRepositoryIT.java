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
import uk.gov.netz.api.workflow.request.application.taskview.RequestInfoDTO;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RequestRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestRepository requestRepository;
    
    @Test
    void findByIdForUpdate() {
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request request = createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "CANCELLED", LocalDateTime.now());
        
        flushAndClear();
        
        Optional<Request> result = requestRepository.findByIdForUpdate(request.getId());
        
        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isEqualTo(request.getId());
    }
    
    @Test
    void findByProcessInstanceId() {
    	String processInstanceId = "prInId";
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request request = createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, processInstanceId, "IN_PROGRESS", LocalDateTime.now());
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "CANCELLED", LocalDateTime.now());
        
        flushAndClear();
        
        Request result = requestRepository.findByProcessInstanceId(processInstanceId);
        
        assertThat(result).isEqualTo(request);
    }
    
    @Test
    void findByAccountIdAndStatus() {
        Long accountId = 1L;
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request request = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "CANCELLED", LocalDateTime.now());
        
        flushAndClear();
        
        List<Request> result = requestRepository.findByAccountIdAndStatus(accountId, "IN_PROGRESS");
        
        assertThat(result).containsExactlyInAnyOrder(request);
    }

    @Test
    void findAllByAccountId() {
        Long accountId1 = 1L;
        Long accountId2 = 2L;
        
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request acc1Request = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
    	
        flushAndClear();

        List<Request> retrievedRequests = requestRepository.findAllByAccountId(accountId1);

        assertThat(retrievedRequests).hasSize(1);
        assertThat(retrievedRequests)
            .extracting(Request::getId)
            .containsExactly(acc1Request.getId());
    }

    @Test
    void findAllByAccountIdInAndStatusIn() {
        Long accountId1 = 1L;
        Long accountId2 = 2L;
        Long accountId3 = 3L;
        
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request acc1Request = createRequest(accountId1, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "APPROVED", LocalDateTime.now());
    	Request acc2Request = createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(accountId3, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
    	
        flushAndClear();

        List<Request> retrievedRequests = requestRepository.findAllByAccountIdIn(Set.of(accountId1, accountId2));

        assertThat(retrievedRequests).hasSize(2);
        assertThat(retrievedRequests)
            .extracting(Request::getId)
            .containsExactlyInAnyOrder(acc1Request.getId(), acc2Request.getId());
    }
    
    @Test
    void existsByTypeAndStatusAndCompetentAuthority_exists() {
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	
        flushAndClear();
        
        boolean result = requestRepository.existsByTypeAndStatusAndCompetentAuthority(requestType.getCode(), "IN_PROGRESS", CompetentAuthorityEnum.ENGLAND);
        
        assertThat(result).isTrue();
    }
    
    @Test
    void existsByTypeAndStatusAndCompetentAuthority_not_exist() {
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "APPROVED", LocalDateTime.now());
    	createRequest(2L, CompetentAuthorityEnum.OPRED, null, requestType, "procInstId2", "IN_PROGRESS", LocalDateTime.now());
    	
        flushAndClear();
        
        boolean result = requestRepository.existsByTypeAndStatusAndCompetentAuthority(requestType.getCode(), "IN_PROGRESS", CompetentAuthorityEnum.ENGLAND);
        
        assertThat(result).isFalse();
    }

    @Test
    void findByAccountIdAndTypeAndStatus() {
        Long accountId = 1L;
        Long accountId2 = 2L;
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        RequestType requestType2 = createRequestType("DUMMY_REQUEST_TYPE2", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        Request request = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "APPROVED", LocalDateTime.now());
        createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId2", "APPROVED", LocalDateTime.now());
        createRequest(accountId2, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId3", "APPROVED", LocalDateTime.now());
        createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId4", "CANCELLED", LocalDateTime.now());

        flushAndClear();

        List<Request> result = requestRepository.findByAccountIdAndTypeAndStatus(accountId, "DUMMY_REQUEST_TYPE", "APPROVED");

        assertThat(result).containsExactlyInAnyOrder(request);
    }
    
    @Test
    void findByResourceAndStatus() {
        Long accountId = 1L;
        RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
    	Request request = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "CANCELLED", LocalDateTime.now());
        
        flushAndClear();
        
        List<Request> result = requestRepository.findByResourceAndStatus(accountId, ResourceType.ACCOUNT, "IN_PROGRESS");
        
        assertThat(result).containsExactlyInAnyOrder(request);
    }

    @Test
    void findByRequestTypeAndResourceTypeAndResourceId() {
        Long accountId = 1L;
        RequestType requestType1 = createRequestType("REQUEST_TYPE_1", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        RequestType requestType2 = createRequestType("REQUEST_TYPE_2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        Request request1 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
        Request request2 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId2", "CANCELLED", LocalDateTime.now());
        createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
        createRequest(null, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId4", "IN_PROGRESS", LocalDateTime.now());

        flushAndClear();

        List<Request> result = requestRepository.findByRequestTypeAndResourceTypeAndResourceId( "REQUEST_TYPE_1", ResourceType.ACCOUNT,  String.valueOf(accountId));

        assertThat(result).containsExactlyInAnyOrder(request1, request2);
    }

    @Test
    void findByResourceTypeAndResourceIdAndTypeNotIn() {
        Long accountId = 1L;
        RequestType requestType1 = createRequestType("REQUEST_TYPE_1", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        RequestType requestType2 = createRequestType("REQUEST_TYPE_2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);

        createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType1, "processInstanceId1", "IN_PROGRESS", LocalDateTime.now());
        Request request2 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType2, "processInstanceId2", "IN_PROGRESS", LocalDateTime.now());
        Request request3 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType2, "processInstanceId3", "CANCELLED", LocalDateTime.now().minusYears(1));

        RequestInfoDTO requestInfoDTO2 = RequestInfoDTO.builder().id(request2.getId()).type(requestType2.getCode()).build();
        RequestInfoDTO requestInfoDTO3 = RequestInfoDTO.builder().id(request3.getId()).type(requestType2.getCode()).build();

        flushAndClear();

        List<RequestInfoDTO> result = requestRepository.findByResourceTypeAndResourceIdAndTypeNotIn(List.of("REQUEST_TYPE_1"), ResourceType.ACCOUNT,  String.valueOf(accountId));

        assertThat(result).containsExactly(requestInfoDTO3, requestInfoDTO2);
    }
}
