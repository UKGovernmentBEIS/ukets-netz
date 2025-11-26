package uk.gov.netz.api.authorization.regulator.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.service.RoleService;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorRolePermissionsDTO;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorRoleMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegulatorRoleService {
    private final RegulatorRoleMapper regulatorRoleMapper;
    private final RoleService roleService;

    /**
     * Returns regulator roles.
     * @return List of RolePermissionsDTO
     */
    public List<RegulatorRolePermissionsDTO> getRegulatorRoles() {
        return roleService.getRegulatorRoles()
                .stream().map(regulatorRoleMapper::toRolePermissionsDTO)
                .collect(Collectors.toList());
    }
}
