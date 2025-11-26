package uk.gov.netz.api.authorization.core.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.domain.dto.RoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.RolePermissionsDTO;
import uk.gov.netz.api.authorization.core.repository.RoleRepository;
import uk.gov.netz.api.authorization.core.transform.RoleMapper;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import static uk.gov.netz.api.common.constants.RoleTypeConstants.REGULATOR;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.VERIFIER;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    private static final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    public Set<String> getCodesByType(String roleType) {
        return roleRepository.findByType(roleType)
                .stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * Returns operator roles.
     * @return List of {@link RoleDTO}
     */
    public List<RoleDTO> getOperatorRoles() {
        return roleRepository.findByType(RoleTypeConstants.OPERATOR).stream()
                .map(roleMapper::toRoleDTO).collect(Collectors.toList());
    }

    /**
     * Returns regulator roles.
     * @return List of RolePermissionsDTO
     */
    public List<RolePermissionsDTO> getRegulatorRoles() {
        return roleRepository.findByType(REGULATOR).stream()
                .map(roleMapper::toRolePermissionsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the Role that corresponds to the provided role code.
     * @param roleCode the role code
     * @return the {@link RoleDTO}
     */
    public RoleDTO getRoleByCode(String roleCode) {
        Role role = getRoleByRoleCode(roleCode);
        return roleMapper.toRoleDTO(role);
    }

    /**
     * Returns all roles that correspond to VERIFIER.
     * @return {@link List} of {@link RoleDTO}
     */
    public List<RoleDTO> getVerifierRoleCodes() {
        return roleRepository.findByType(VERIFIER).stream()
                .map(roleMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    protected Role getRoleByRoleCode(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

}
