package uk.gov.netz.api.authorization.rules.services.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegulatorRequestTaskRoleTypeAuthorizationQueryServiceTest {

    @InjectMocks
    private RegulatorRequestTaskRoleTypeAuthorizationQueryService service;

    @Mock
    private RegulatorAuthorityResourceService regulatorAuthorityResourceService;

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteria_requiresPermission_true() {
        String requestTaskType = "requestTaskType";
        ResourceCriteria resourceCriteria = ResourceCriteria.builder().requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name())).build();
        List<String> users = List.of("user");

        when(regulatorAuthorityResourceService.findUsersWithScopeOnResourceTypeAndSubTypeAndCA(ResourceType.REQUEST_TASK, requestTaskType,
                Scope.REQUEST_TASK_EXECUTE, resourceCriteria.getCompetentAuthority())).thenReturn(users);

        List<String> usersWhoCanExecuteRequestTaskTypeByAccountCriteria = service.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(requestTaskType, resourceCriteria, true);

        assertEquals(users, usersWhoCanExecuteRequestTaskTypeByAccountCriteria);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteria_requiresPermission_false() {
        String requestTaskType = "requestTaskType";
        ResourceCriteria resourceCriteria = ResourceCriteria.builder().requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name())).build();
        List<String> users = List.of("user");

        when(regulatorAuthorityResourceService.findUsersByCompetentAuthority(resourceCriteria.getCompetentAuthority())).thenReturn(users);

        List<String> usersWhoCanExecuteRequestTaskTypeByAccountCriteria = service.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(requestTaskType, resourceCriteria, false);

        assertEquals(users, usersWhoCanExecuteRequestTaskTypeByAccountCriteria);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.REGULATOR, service.getRoleType());

    }

}