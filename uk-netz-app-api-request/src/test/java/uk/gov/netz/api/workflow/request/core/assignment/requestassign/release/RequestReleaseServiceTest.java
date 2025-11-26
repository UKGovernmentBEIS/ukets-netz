package uk.gov.netz.api.workflow.request.core.assignment.requestassign.release;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestReleaseServiceTest {

    @InjectMocks
    private RequestReleaseService cut;

    @Mock
    private AuthorizationRulesQueryService authorizationRulesQueryService;
    
    @Mock
    private UserRoleTypeOperatorRequestReleaseService userRoleTypeOperatorRequestReleaseService;

    @Mock
    private UserRoleTypeRegulatorRequestReleaseService userRoleTypeRegulatorRequestReleaseService;

    @BeforeEach
    void setup() {
		cut = new RequestReleaseService(authorizationRulesQueryService,
				List.of(userRoleTypeOperatorRequestReleaseService, userRoleTypeRegulatorRequestReleaseService));
    }

    @Test
    void releaseRequest() {
        final String assignee = "assignee";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("code").build())
            .assignee(assignee)
            .build();

        when(authorizationRulesQueryService.findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code"))
            .thenReturn(Optional.of(RoleTypeConstants.OPERATOR));
        
        when(userRoleTypeOperatorRequestReleaseService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);

        cut.releaseRequest(requestTask);
        
        verify(authorizationRulesQueryService, times(1))
            .findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code");
        verify(userRoleTypeOperatorRequestReleaseService, times(1)).getRoleType();
        verify(userRoleTypeOperatorRequestReleaseService, times(1)).release(request, assignee);
    }


    @Test
    void releaseRequest_null_role_type() {
        final String assignee = "assignee";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("code").build())
            .assignee(assignee)
            .build();

        when(authorizationRulesQueryService.findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code"))
            .thenReturn(Optional.empty());

        cut.releaseRequest(requestTask);

        verify(authorizationRulesQueryService, times(1))
            .findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code");
        verifyNoInteractions(userRoleTypeOperatorRequestReleaseService, userRoleTypeRegulatorRequestReleaseService);
    }
    
    @Test
    void releaseRequest_throws_exception_when_implementation_not_found() {
    	final String assignee = "assignee";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("code").build())
            .assignee(assignee)
            .build();

        when(authorizationRulesQueryService.findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code"))
            .thenReturn(Optional.of(RoleTypeConstants.VERIFIER));
        
        when(userRoleTypeOperatorRequestReleaseService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(userRoleTypeRegulatorRequestReleaseService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> cut.releaseRequest(requestTask));
        assertThat(ex.getMessage()).isEqualTo("User with role type VERIFIER not related with request assignment");

        verify(authorizationRulesQueryService, times(1))
            .findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, "code");
        verify(userRoleTypeOperatorRequestReleaseService, times(1)).getRoleType();
        verify(userRoleTypeRegulatorRequestReleaseService, times(1)).getRoleType();
        verifyNoMoreInteractions(userRoleTypeOperatorRequestReleaseService, userRoleTypeRegulatorRequestReleaseService);

    }

}
