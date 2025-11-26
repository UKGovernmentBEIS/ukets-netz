package uk.gov.netz.api.authorization.regulator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.authorization.core.domain.dto.RolePermissionsDTO;
import uk.gov.netz.api.authorization.core.service.RoleService;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorRolePermissionsDTO;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorPermissionsAdapter;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorRoleMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;

@ExtendWith(MockitoExtension.class)
class RegulatorRoleServiceTest {
    @InjectMocks
    private RegulatorRoleService regulatorRoleService;

    @Mock
    private RoleService roleService;

    @Mock
    private RegulatorPermissionsAdapter regulatorPermissionsAdapter;

    @Mock
    private RegulatorRoleMapper regulatorRoleMapper;

    @Test
    void getRegulatorRoles() {
        RolePermissionsDTO role1 = RolePermissionsDTO.builder()
                .code("code1")
                .rolePermissions(List.of(RolePermission.builder()
                        .permission(PERM_TASK_ASSIGNMENT)
                        .build()))
                .build();
        RolePermissionsDTO role2 = RolePermissionsDTO.builder()
                .code("code2")
                .rolePermissions(List.of(RolePermission.builder()
                        .permission(PERM_ACCOUNT_USERS_EDIT).
                                build()))
                .build();

        RegulatorRolePermissionsDTO regulatorRolePermissionsDTO1 = RegulatorRolePermissionsDTO.builder()
                .code("code1")
                .rolePermissions(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(List.of(PERM_TASK_ASSIGNMENT)))
                .build();
        RegulatorRolePermissionsDTO regulatorRolePermissionsDTO2 = RegulatorRolePermissionsDTO.builder()
                .code("code2")
                .rolePermissions(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(List.of(PERM_ACCOUNT_USERS_EDIT)))
                .build();

        when(regulatorRoleMapper.toRolePermissionsDTO(role1)).thenReturn(regulatorRolePermissionsDTO1);
        when(regulatorRoleMapper.toRolePermissionsDTO(role2)).thenReturn(regulatorRolePermissionsDTO2);

        when(roleService.getRegulatorRoles()).thenReturn(List.of(role1, role2));

        List<RegulatorRolePermissionsDTO> regulatorRolePermissionsDTOS = regulatorRoleService.getRegulatorRoles();

        assertThat(regulatorRolePermissionsDTOS).hasSameElementsAs(List.of(regulatorRolePermissionsDTO1, regulatorRolePermissionsDTO2));
    }
}