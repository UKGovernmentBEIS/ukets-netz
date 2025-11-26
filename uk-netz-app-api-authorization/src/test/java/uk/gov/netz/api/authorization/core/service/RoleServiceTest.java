package uk.gov.netz.api.authorization.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.authorization.core.domain.dto.RoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.RolePermissionsDTO;
import uk.gov.netz.api.authorization.core.repository.RoleRepository;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.REGULATOR;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Test
    void getCodesByType() {
        List<Role> operatorRoles = List.of(
                Role.builder().code("code1").type(RoleTypeConstants.OPERATOR).name("name1").build(),
                Role.builder().code("code2").type(RoleTypeConstants.OPERATOR).name("name2").build(),
                Role.builder().code("code3").type(RoleTypeConstants.OPERATOR).name("name3").build()
        );
        when(roleRepository.findByType(RoleTypeConstants.OPERATOR)).thenReturn(operatorRoles);

        //invoke
        Set<String> codes = roleService.getCodesByType(RoleTypeConstants.OPERATOR);

        //assert
        assertThat(codes).containsExactlyInAnyOrder("code1", "code2", "code3");
    }

    @Test
    void getOperatorRoles() {
        final String code1 = "code1";
        final String code2 = "code2";
        List<RoleDTO> roleDTOS = List.of(buildRoleDTO(code1), buildRoleDTO(code2));
        List<Role> roles = List.of(buildRole(code1), buildRole(code2));

        when(roleRepository.findByType(RoleTypeConstants.OPERATOR)).thenReturn(roles);

        // Invoke
        List<RoleDTO> actual = roleService.getOperatorRoles();

        // Assert
        verify(roleRepository, times(1)).findByType(RoleTypeConstants.OPERATOR);
        verify(roleRepository, never()).findByCode(anyString());
        assertEquals(roleDTOS, actual);
    }

    @Test
    void getRegulatorRoles() {
        Role role1 = buildRole("code1", PERM_TASK_ASSIGNMENT);
        Role role2 = buildRole("code2", PERM_ACCOUNT_USERS_EDIT);
        when(roleRepository.findByType(REGULATOR)).thenReturn(List.of(role1, role2));

        List<RolePermissionsDTO> rolePermissionsDTOS = roleService.getRegulatorRoles();
        assertThat(rolePermissionsDTOS).hasSameElementsAs(List.of(buildRolePermissionsDTO(role1), buildRolePermissionsDTO(role2)));
    }

    @Test
    void getRoleByCode() {
        final String roleCode = "operator";
        Role role = buildRole(roleCode);
        RoleDTO expectedRoleDTO = buildRoleDTO(roleCode);

        when(roleRepository.findByCode(roleCode)).thenReturn(Optional.of(role));

        RoleDTO actualRoleDTO = roleService.getRoleByCode(roleCode);

        assertEquals(expectedRoleDTO, actualRoleDTO);
    }

    @Test
    void getRoleByCodeThrowsExceptionWhenResourceNotFound() {
        final String roleCode = "operator";

        when(roleRepository.findByCode(roleCode)).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> roleService.getRoleByCode(roleCode));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
    }

    @Test
    void getVerifierRoleCodes() {
        final String code1 = "code1";
        final String code2 = "code2";
        List<RoleDTO> expectedRoleDTOs = List.of(buildRoleDTO(code1), buildRoleDTO(code2));
        List<Role> roles = List.of(buildRole(code1), buildRole(code2));

        when(roleRepository.findByType(RoleTypeConstants.VERIFIER)).thenReturn(roles);

        // Invoke
        List<RoleDTO> actualRoleDTOs = roleService.getVerifierRoleCodes();

        // Assert
        verify(roleRepository, times(1)).findByType(RoleTypeConstants.VERIFIER);
        assertEquals(expectedRoleDTOs, actualRoleDTOs);
    }

    private RoleDTO buildRoleDTO(String code) {
        return RoleDTO.builder()
                .code(code)
                .build();
    }

    private Role buildRole(String code, String... permissions) {
        Role role = Role.builder()
                .code(code)
                .build();

        Arrays.stream(permissions).forEach(p ->
                role.addPermission(
                        RolePermission.builder()
                                .permission(p).build()));

        return role;
    }

    private RolePermissionsDTO buildRolePermissionsDTO(Role role) {
        RolePermissionsDTO rolePermissionDTO = RolePermissionsDTO.builder()
                .code(role.getCode())
                .rolePermissions(role.getRolePermissions())
                .build();
        return rolePermissionDTO;
    }

}
