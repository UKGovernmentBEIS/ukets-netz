package uk.gov.netz.api.workflow.request.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsDTO;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsSearchResults;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestSearchCriteria;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, RequestDetailsRepository.class})
class RequestDetailsRepositoryIT extends RequestAbstractTest {

    @Autowired
    private RequestDetailsRepository repo;
    
    @Test
    void findRequestDetailsBySearchCriteria_filter_with_category_and_request_types_criteria_only() {
        Long accountId = 1L;
        AppUser user = AppUser.builder().roleType("REGULATOR").build();
        
        RequestType requestType1 = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        RequestType requestType2 = createRequestType("DUMMY_REQUEST_TYPE2", "descr2", "processdef2", "REPORTING", false, true, false, false, ResourceType.CA);
        RequestType requestType3 = createRequestType("DUMMY_REQUEST_TYPE3", "descr3", "processdef3", "REPORTING", false, true, false, false, ResourceType.ACCOUNT);
        
        Request request1 = createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(2L, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId2", "COMPLETED", LocalDateTime.now());
    	createRequest(2L, CompetentAuthorityEnum.ENGLAND, null, requestType1, "procInstId3", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType2, "procInstId4", "COMPLETED", LocalDateTime.now());
    	createRequest(accountId, CompetentAuthorityEnum.ENGLAND, null, requestType3, "procInstId5", "IN_PROGRESS", LocalDateTime.now());
    	
    	createAuthorizationRuleForRequest("REQUEST", "DUMMY_REQUEST_TYPE", "requestAccessHandler", null, "REGULATOR");
    	createAuthorizationRuleForRequest("REQUEST", "DUMMY_REQUEST_TYPE2", "requestAccessHandler", null, "REGULATOR");
    	createAuthorizationRuleForRequest("REQUEST", "DUMMY_REQUEST_TYPE3", "requestAccessHandler", null, "OPERATOR");
        
        flushAndClear();
        
        RequestSearchCriteria criteria = RequestSearchCriteria.builder()
        		.resourceType(ResourceType.ACCOUNT)
                .resourceId(String.valueOf(accountId))
                .paging(PagingRequest.builder().pageNumber(0).pageSize(30).build())
                .historyCategory("REPORTING")
                .requestTypes(Set.of("DUMMY_REQUEST_TYPE", "DUMMY_REQUEST_TYPE2", "DUMMY_REQUEST_TYPE3"))
                .build();
        
        RequestDetailsSearchResults results = repo.findRequestDetailsBySearchCriteria(criteria, user);
        
        RequestDetailsDTO expectedWorkflowResult1 = new RequestDetailsDTO(request1.getId(), requestType1.getCode(), request1.getStatus(), request1.getCreationDate(), null);

        assertThat(results).isNotNull();
        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getRequestDetails()).isEqualTo(List.of(expectedWorkflowResult1));
    }

    @Test
    void findRequestDetailsById() {
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.CA);
    	Request request = createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "IN_PROGRESS", LocalDateTime.now());
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId2", "COMPLETED", LocalDateTime.now());
    	
        flushAndClear();

        Optional<RequestDetailsDTO> actualOpt = repo.findRequestDetailsById(request.getId());

        assertThat(actualOpt).isNotEmpty();
        RequestDetailsDTO actual = actualOpt.get();
        assertThat(actual.getId()).isEqualTo(request.getId());
        assertThat(actual.getRequestType()).isEqualTo(request.getType().getCode());
        assertThat(actual.getRequestStatus()).isEqualTo(request.getStatus());
        assertThat(actual.getCreationDate()).isEqualTo(request.getCreationDate().toLocalDate());
    }
    
    @Test
    void findRequestDetailsById_not_found() {
    	RequestType requestType = createRequestType("DUMMY_REQUEST_TYPE", "descr1", "processdef1", "REPORTING", false, true, false, false, ResourceType.CA);
    	createRequest(1L, CompetentAuthorityEnum.ENGLAND, null, requestType, "procInstId1", "COMPLETED", LocalDateTime.now());
        flushAndClear();

        Optional<RequestDetailsDTO> actualOpt = repo.findRequestDetailsById("invalid_request_id");

        assertThat(actualOpt).isEmpty();
    }

}