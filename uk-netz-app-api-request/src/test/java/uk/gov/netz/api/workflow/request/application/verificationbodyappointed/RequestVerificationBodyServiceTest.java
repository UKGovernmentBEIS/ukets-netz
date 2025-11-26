package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestVerificationBodyServiceTest {
    
    @InjectMocks
    private RequestVerificationBodyService service;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private RequestService requestService;

    @Test
    void appointVerificationBodyToRequestsOfAccount() {
        Long accountId = 1L;
        Long vbIdOld = 11L;
        Long vbIdNew = 21L;
        String verifierAssignee = "verifierAssignee";
        Request request1 = Request.builder().build();
        addResourcesToRequest(accountId, vbIdOld, request1);
        Request request2 = Request.builder()
            .payload(TestRequestPayload.builder().verifierAssignee(verifierAssignee).build())
            .build();
        addResourcesToRequest(accountId, vbIdOld, request2);

        when(requestRepository.findAllByAccountId(accountId)).thenReturn(List.of(request1, request2));

        service.appointVerificationBodyToRequestsOfAccount(vbIdNew, accountId);

        assertEquals(request1.getVerificationBodyId(), vbIdNew);
        assertEquals(request2.getVerificationBodyId(), vbIdNew);
        assertNull(request2.getPayload().getVerifierAssignee());
        verifyNoInteractions(workflowService);
    }

    @Test
    void unappointVerificationBodyFromRequestsOfAccounts() {
        Long accountId = 1L;
        Long verificationBodyId = 11L;
        String verifierAssignee = "verifierAssignee";
        Request request1 = Request.builder().build();
        addResourcesToRequest(accountId, verificationBodyId, request1);
        Request request2 = Request.builder()
            .payload(TestRequestPayload.builder().verifierAssignee(verifierAssignee).build())
            .build();
        addResourcesToRequest(accountId, verificationBodyId, request2);

        when(requestRepository.findAllByAccountIdIn(Set.of(accountId))).thenReturn(List.of(request1, request2));

        service.unappointVerificationBodyFromRequestsOfAccounts(Set.of(accountId));

        assertNull(request1.getVerificationBodyId());
        assertNull(request2.getVerificationBodyId());
        assertNull(request2.getPayload().getVerifierAssignee());
        verifyNoInteractions(workflowService);
        verifyNoInteractions(requestService);
    }
    
    private void addResourcesToRequest(Long accountId, Long vbId, Request request) {	
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();
		
		RequestResource vbIdResource = RequestResource.builder()
				.resourceType(ResourceType.VERIFICATION_BODY)
				.resourceId(vbId.toString())
				.request(request)
				.build();
        request.getRequestResources().addAll(List.of(accountResource, vbIdResource));
	}
}
