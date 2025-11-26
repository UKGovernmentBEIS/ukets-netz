package uk.gov.netz.api.workflow.request.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.bpmn.WorkflowEngineType;
import uk.gov.netz.api.workflow.bpmn.WorkflowTypeServiceDelegator;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCreateServiceTest {

    @InjectMocks
    private RequestCreateService service;

    @Mock
    private RequestRepository requestRepository;
    
    @Mock
    private RequestTypeRepository requestTypeRepository;
    
    @Mock
    private AccountQueryService accountQueryService;
    
    @Mock
    private WorkflowTypeServiceDelegator workflowTypeServiceDelegator;
    
    @Test
    void createRequest_with_accountId() {
    	final RequestType type = RequestType.builder().code("code").build();
        final String status = "IN_PROGRESS";
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final Long accountId = 1L;
        final Long verificationBodyId = 1L;

        when(requestTypeRepository.findByCode("code")).thenReturn(Optional.of(type));
        when(accountQueryService.getAccountCa(accountId)).thenReturn(ca);
        when(accountQueryService.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(verificationBodyId));
        when(workflowTypeServiceDelegator.getWorkflowEngineByType(type.getCode())).thenReturn(WorkflowEngineType.CAMUNDA);
    	
        RequestParams requestParams = RequestParams.builder()
            .requestId("1")
            .type(type.getCode())
            .requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
            .build();
        //invoke
        service.createRequest(requestParams, status);
        
        //verify
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository, times(1)).save(requestCaptor.capture());
        Request request = requestCaptor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.getType()).isEqualTo(type);
        assertThat(request.getEngine()).isEqualTo(WorkflowEngineType.CAMUNDA);
        assertThat(request.getStatus()).isEqualTo(status);
        assertThat(request.getCompetentAuthority()).isEqualTo(ca);
        assertThat(request.getVerificationBodyId()).isEqualTo(verificationBodyId);
        assertThat(request.getAccountId()).isEqualTo(accountId);

        verify(requestTypeRepository, times(1)).findByCode("code");
        verify(accountQueryService, times(1)).getAccountCa(accountId);
        verify(accountQueryService, times(1)).getAccountVerificationBodyId(accountId);
        verify(workflowTypeServiceDelegator, times(1)).getWorkflowEngineByType(type.getCode());
    }
    
    @Test
    void createRequest_with_comp_authority() {
    	final RequestType type = RequestType.builder().code("code").build();
        final String status = "IN_PROGRESS";
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        
        when(requestTypeRepository.findByCode("code")).thenReturn(Optional.of(type));
        when(workflowTypeServiceDelegator.getWorkflowEngineByType(type.getCode())).thenReturn(WorkflowEngineType.CAMUNDA);

        RequestParams requestParams = RequestParams.builder()
            .requestId("1")
            .type(type.getCode())
            .requestResources(Map.of(ResourceType.CA, ca.name()))
            .build();
        //invoke
        service.createRequest(requestParams, status);
        
        //verify
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository, times(1)).save(requestCaptor.capture());
        Request request = requestCaptor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.getType()).isEqualTo(type);
        assertThat(request.getEngine()).isEqualTo(WorkflowEngineType.CAMUNDA);
        assertThat(request.getStatus()).isEqualTo(status);
        assertThat(request.getCompetentAuthority()).isEqualTo(ca);
        assertThat(request.getVerificationBodyId()).isNull();

        verify(requestTypeRepository, times(1)).findByCode("code");
        verify(workflowTypeServiceDelegator, times(1)).getWorkflowEngineByType(type.getCode());
        verifyNoInteractions(accountQueryService);
    }
}
