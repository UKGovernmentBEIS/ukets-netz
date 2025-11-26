package uk.gov.netz.api.workflow.request.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @InjectMocks
    private RequestService requestService;

    @Mock
    private RequestRepository requestRepository;
    
    @Mock
    private RequestActionUserInfoResolver requestActionUserInfoResolver;
    
    @Test
    void addActionToRequest() {
    	final String user = "user";
        Request request = createRequest("1", "1", 1L, "requestTypeCode");
        RequestActionPayload payload = Mockito.mock(RequestActionPayload.class);
        
        //assert before
        assertThat(request.getRequestActions()).isEmpty();
        
        when(requestActionUserInfoResolver.getUserFullName(user)).thenReturn("submitter name");
        
        //invoke
        requestService.addActionToRequest(request, 
        		payload, 
        		"code", 
        		user);
        
        //verify
        verify(requestActionUserInfoResolver, times(1)).getUserFullName(user);
        
        //assert
        assertThat(request.getRequestActions()).hasSize(1);
        assertThat(request.getRequestActions()).extracting(RequestAction::getType).containsOnly("code");
        assertThat(request.getRequestActions()).extracting(RequestAction::getPayload).containsOnly(payload);
    }

    @Test
    void updateRequestStatus() {
        Request request = Request.builder().id("1").status("IN_PROGRESS").build();

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        // Invoke
        requestService.updateRequestStatus(request.getId(), "APPROVED");

        // Verify
        assertEquals(request.getStatus(), "APPROVED");
    }
    
    @Test
    void terminateRequest() {
    	//prepare data
        String processId = "1";
        Request request = createRequest("1", processId, 1L, "requestTypeCode");
        request.setPayload(TestRequestPayload.builder()
            .payloadType("TEST_REQUEST_PAYLOAD")
        .build()
        );
        request.setStatus("IN_PROGRESS");
        
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        
        //assert before
        assertThat(request.getStatus()).isEqualTo("IN_PROGRESS");
        
        //invoke
        requestService.terminateRequest(request.getId(), processId, false);
        
        //assert
        assertThat(request.getStatus()).isEqualTo("COMPLETED");
        assertThat(request.getPayload()).isNull();

        //verify
        verify(requestRepository, never()).delete(request);
    }
    
    @Test
    void terminateRequest_should_delete_request() {
        String processId = "1";
        Request request = createRequest("1", processId, 1L, "code");

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        //invoke
        requestService.terminateRequest(request.getId(), processId, true);

        //verify
        verify(requestRepository, times(1)).delete(request);
    }

    @Test
    void terminateRequest_not_same_process_instance() {
        //prepare data
        String processId = "1";
        Request request = createRequest("1","2", 1L, "requestTypeCode");
        request.setStatus("IN_PROGRESS");

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        //invoke
        requestService.terminateRequest(request.getId(), processId, false);

        //verify
        verifyNoMoreInteractions(requestRepository);
    }
    
    private Request createRequest(String requestId, String processInstanceId, Long accountId, String type) {
        Request request = new Request();
        request.setId(requestId);
        request.setProcessInstanceId(processInstanceId);
        request.setType(RequestType.builder().code(type).build());
        return request;
    }
}
