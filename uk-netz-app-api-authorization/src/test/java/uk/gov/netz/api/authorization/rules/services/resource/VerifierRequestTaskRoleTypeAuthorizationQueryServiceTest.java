package uk.gov.netz.api.authorization.rules.services.resource;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifierRequestTaskRoleTypeAuthorizationQueryServiceTest {
    @InjectMocks
    private VerifierRequestTaskRoleTypeAuthorizationQueryService service;

    @Mock
    private VerifierAuthorityResourceService verifierAuthorityResourceService;

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteria_requiresPermission_true() {
        String requestTaskType = "requestTaskType";
        Long verificationBodyId = 1L;
        ResourceCriteria resourceCriteria = ResourceCriteria.builder().requestResources(Map.of(ResourceType.VERIFICATION_BODY, verificationBodyId.toString())).build();
        List<String> users = List.of("user");

        when(verifierAuthorityResourceService.findUsersWithScopeOnResourceTypeAndSubTypeAndVerificationBodyId(ResourceType.REQUEST_TASK, requestTaskType,
                Scope.REQUEST_TASK_EXECUTE, resourceCriteria.getVerificationBodyId())).thenReturn(users);

        List<String> usersWhoCanExecuteRequestTaskTypeByAccountCriteria = service.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(requestTaskType, resourceCriteria, true);

        assertEquals(users, usersWhoCanExecuteRequestTaskTypeByAccountCriteria);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteria_requiresPermission_false() {
        String requestTaskType = "requestTaskType";
        Long verificationBodyId = 1L;
        ResourceCriteria resourceCriteria = ResourceCriteria.builder().requestResources(Map.of(ResourceType.VERIFICATION_BODY, verificationBodyId.toString())).build();
        List<String> users = List.of("user");

        when(verifierAuthorityResourceService.findUsersByVerificationBodyId(resourceCriteria.getVerificationBodyId())).thenReturn(users);

        List<String> usersWhoCanExecuteRequestTaskTypeByAccountCriteria = service.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(requestTaskType, resourceCriteria, false);

        assertEquals(users, usersWhoCanExecuteRequestTaskTypeByAccountCriteria);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.VERIFIER, service.getRoleType());

    }

}