package uk.gov.netz.api.authorization.core.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.authorization.core.domain.UserRoleType;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.common.config.MapperConfig;

/**
 * Mapper for {@link UserRoleType} objects.
 */
@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface UserRoleTypeMapper {

    UserRoleTypeDTO toUserRoleTypeDTO(UserRoleType userRoleType);
}
