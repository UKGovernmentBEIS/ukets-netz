package uk.gov.netz.api.authorization.regulator.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.authorization.core.domain.dto.RolePermissionsDTO;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorRolePermissionsDTO;
import uk.gov.netz.api.common.config.MapperConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = RegulatorPermissionsAdapter.class, config = MapperConfig.class)
public interface RegulatorRoleMapper {

    @Mapping(source = "rolePermissions", target = "rolePermissions", qualifiedByName = "getPermissionGroupLevelsFromRolePermissions")
    RegulatorRolePermissionsDTO toRolePermissionsDTO(RolePermissionsDTO role);

}
