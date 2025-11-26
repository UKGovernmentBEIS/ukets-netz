package uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class RequestAssignmentServiceTest {
    
    @InjectMocks
    private RequestAssignmentService cut;
    
    @Mock
    private UserRoleTypeService userRoleTypeService;
    
    @Mock
    private UserRoleTypeOperatorRequestAssignmentService userRoleTypeOperatorRequestAssignmentService;

    @Mock
    private UserRoleTypeRegulatorRequestAssignmentService userRoleTypeRegulatorRequestAssignmentService;

    @BeforeEach
    void setup() {
		cut = new RequestAssignmentService(userRoleTypeService,
				List.of(userRoleTypeOperatorRequestAssignmentService, userRoleTypeRegulatorRequestAssignmentService));
    }

    @Test
    void assignRequestToUser() {
        Request request = Request.builder().build();
        String userId = "operatorUser";
        
        UserRoleTypeDTO candidateAssigneeRoleType = UserRoleTypeDTO.builder()
            .userId(userId)
            .roleType(RoleTypeConstants.OPERATOR)
            .build();

        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(candidateAssigneeRoleType);
        when(userRoleTypeOperatorRequestAssignmentService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);

        cut.assignRequestToUser(request, userId);

        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserId(userId);
        verify(userRoleTypeOperatorRequestAssignmentService, times(1)).assign(request, userId);
        verify(userRoleTypeOperatorRequestAssignmentService, times(1)).getRoleType();
    }
    
    @Test
    void assignRequestToUser_throws_exception_when_implementation_not_found() {
        Request request = Request.builder().build();
        String userId = "user";
        
        UserRoleTypeDTO candidateAssigneeRoleType = UserRoleTypeDTO.builder()
            .userId(userId)
            .roleType(RoleTypeConstants.VERIFIER)
            .build();

        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(candidateAssigneeRoleType);
        when(userRoleTypeOperatorRequestAssignmentService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(userRoleTypeRegulatorRequestAssignmentService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> cut.assignRequestToUser(request, userId));
        assertThat(ex.getMessage()).isEqualTo("User with role type VERIFIER not related with request assignment");

        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserId(userId);
        verify(userRoleTypeOperatorRequestAssignmentService, never()).assign(request, userId);
        verify(userRoleTypeOperatorRequestAssignmentService, times(1)).getRoleType();
        verify(userRoleTypeRegulatorRequestAssignmentService, never()).assign(request, userId);
        verify(userRoleTypeRegulatorRequestAssignmentService, times(1)).getRoleType();
    }

}
