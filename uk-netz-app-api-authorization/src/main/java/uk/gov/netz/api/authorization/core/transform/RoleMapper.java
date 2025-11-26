package uk.gov.netz.api.authorization.core.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.domain.dto.RoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.RolePermissionsDTO;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface RoleMapper {

    RoleDTO toRoleDTO(Role role);

    RolePermissionsDTO toRolePermissionsDTO(Role role);

}
