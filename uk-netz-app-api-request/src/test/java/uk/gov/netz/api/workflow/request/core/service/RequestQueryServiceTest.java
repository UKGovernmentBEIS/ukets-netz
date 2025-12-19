package uk.gov.netz.api.workflow.request.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.taskview.RequestInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsDTO;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestDetailsSearchResults;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestSearchCriteria;
import uk.gov.netz.api.workflow.request.core.repository.RequestDetailsRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestQueryServiceTest {

    @InjectMocks
    private RequestQueryService service;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestDetailsRepository requestDetailsRepository;
    
    @Test
    void findInProgressRequestsByAccount() {
        Long accountId = 1L;
        Request request = Request.builder().id("1").status("IN_PROGRESS").build();

        when(requestRepository.findByAccountIdAndStatus(accountId, "IN_PROGRESS")).thenReturn(List.of(request));
        
        List<Request> result = service.findInProgressRequestsByAccount(accountId);
        
        assertThat(result).containsExactlyInAnyOrder(request);
        verify(requestRepository, times(1)).findByAccountIdAndStatus(accountId, "IN_PROGRESS");
    }
    
    @Test
    void findByProcessInstanceId() {
    	String processInstanceId = "1";
    	Request request = Request.builder().id("1").processInstanceId(processInstanceId).build();

        when(requestRepository.findByProcessInstanceId(processInstanceId)).thenReturn(request);

        Request result = service.findByProcessInstanceId(processInstanceId);

        assertThat(result).isEqualTo(request);
        verify(requestRepository, times(1)).findByProcessInstanceId(processInstanceId);
    }

    @Test
    void existsRequestById() {
        String requestId = "requestId";

        when(requestRepository.existsById(requestId)).thenReturn(true);

        boolean result = service.existsRequestById(requestId);

        assertThat(result).isTrue();
        verify(requestRepository, times(1)).existsById(requestId);
    }

    @Test
    void existsRequestByAccountAndType() {
        final long accountId = 1L;
        final String requestType = "code";

        when(requestRepository.existsByAccountIdAndType(accountId, requestType)).thenReturn(true);

        boolean result = service.existsRequestByAccountAndType(accountId, requestType);

        assertThat(result).isTrue();
        verify(requestRepository, times(1)).existsByAccountIdAndType(accountId, requestType);
    }
    
    @Test
    void existByRequestTypeAndRequestStatusAndCompetentAuthority() {
    	final String type = "code";
    	String status = "IN_PROGRESS";
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;

        when(requestRepository.existsByTypeAndStatusAndCompetentAuthority(type, status, competentAuthority)).thenReturn(true);

        boolean result = service.existByRequestTypeAndRequestStatusAndCompetentAuthority(type, status, competentAuthority);

        assertThat(result).isTrue();
        verify(requestRepository, times(1)).existsByTypeAndStatusAndCompetentAuthority(type, status, competentAuthority);
    }

    @Test
    void findRequestDetailsBySearchCriteria() {
        Long accountId = 1L;
        final String requestId = "1";
        AppUser user = AppUser.builder().roleType("REGULATOR").build();
        RequestSearchCriteria criteria = RequestSearchCriteria.builder().resourceId(String.valueOf(accountId)).resourceType(ResourceType.ACCOUNT)
        		.paging(PagingRequest.builder().pageNumber(0).pageSize(30).build()).build();

        RequestDetailsDTO workflowResult1 = new RequestDetailsDTO(requestId, "code1", "IN_PROGRESS", LocalDateTime.now(), null);
        RequestDetailsDTO workflowResult2 = new RequestDetailsDTO(requestId, "code1", "IN_PROGRESS", LocalDateTime.now(), null);

        RequestDetailsSearchResults expectedResults = RequestDetailsSearchResults.builder()
                .requestDetails(List.of(workflowResult1, workflowResult2))
                .total(10L)
                .build();

        when(requestDetailsRepository.findRequestDetailsBySearchCriteria(criteria, user)).thenReturn(expectedResults);

        RequestDetailsSearchResults actualResults = service.findRequestDetailsBySearchCriteria(criteria, user);

        assertThat(actualResults).isEqualTo(expectedResults);
        verify(requestDetailsRepository, times(1)).findRequestDetailsBySearchCriteria(criteria, user);
    }

    @Test
    void findRequestDetailsById() {
        final String requestId = "1";
        RequestDetailsDTO expected = new RequestDetailsDTO(requestId, "code1", "IN_PROGRESS", LocalDateTime.now(), null);

        when(requestDetailsRepository.findRequestDetailsById(requestId)).thenReturn(Optional.of(expected));

        RequestDetailsDTO actual = service.findRequestDetailsById(requestId);

        assertThat(actual).isEqualTo(expected);
        verify(requestDetailsRepository, times(1)).findRequestDetailsById(requestId);
    }
    
    @Test
    void findRequestDetailsById_not_found() {
        final String requestId = "1";

        when(requestDetailsRepository.findRequestDetailsById(requestId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () -> service.findRequestDetailsById(requestId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(requestDetailsRepository, times(1)).findRequestDetailsById(requestId);
    }
    
    @Test
    void findInProgressRequestsByResource() {
        Long accountId = 1L;
        Request request = Request.builder().id("1").status("IN_PROGRESS").build();

        when(requestRepository.findByResourceAndStatus(accountId, ResourceType.ACCOUNT, "IN_PROGRESS")).thenReturn(List.of(request));
        
        List<Request> result = service.findInProgressRequestsByResource(accountId, ResourceType.ACCOUNT);
        
        assertThat(result).containsExactlyInAnyOrder(request);
        verify(requestRepository, times(1)).findByResourceAndStatus(accountId, ResourceType.ACCOUNT, "IN_PROGRESS");
    }

    @Test
    void findRequestsByRequestTypeAndResourceTypeAndResourceId() {
        Long accountId = 1L;
        String requestType = "REQUEST_TYPE";
        Request request = Request.builder()
                .id("1")
                .status("IN_PROGRESS")
                .type(RequestType.builder()
                        .code(requestType)
                        .build())
                .build();

        when(requestRepository.findByRequestTypeAndResourceTypeAndResourceId(requestType, ResourceType.ACCOUNT, String.valueOf(accountId))).thenReturn(List.of(request));

        List<Request> result = service.findRequestsByRequestTypeAndResourceTypeAndResourceId(requestType, ResourceType.ACCOUNT, String.valueOf(accountId));

        assertThat(result).containsExactlyInAnyOrder(request);
        verify(requestRepository, times(1)).findByRequestTypeAndResourceTypeAndResourceId(requestType, ResourceType.ACCOUNT, String.valueOf(accountId));
    }

    @Test
    void findByResourceTypeAndResourceIdAndTypeNotIn() {
        Long accountId = 1L;
        List<String> requestType = List.of("REQUEST_TYPE");
        RequestInfoDTO request = mock(RequestInfoDTO.class);

        when(requestRepository.findByResourceTypeAndResourceIdAndTypeNotIn(requestType, ResourceType.ACCOUNT, String.valueOf(accountId))).thenReturn(List.of(request));

        List<RequestInfoDTO> result = service.findByResourceTypeAndResourceIdAndTypeNotIn(requestType, ResourceType.ACCOUNT, String.valueOf(accountId));

        assertThat(result).containsExactlyInAnyOrder(request);

        verify(requestRepository).findByResourceTypeAndResourceIdAndTypeNotIn(requestType, ResourceType.ACCOUNT, String.valueOf(accountId));
        verifyNoMoreInteractions(requestRepository);
        verifyNoMoreInteractions(requestDetailsRepository);
    }
}
