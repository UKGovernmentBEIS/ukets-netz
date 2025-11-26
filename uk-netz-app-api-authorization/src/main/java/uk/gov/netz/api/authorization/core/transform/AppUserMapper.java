package uk.gov.netz.api.authorization.core.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.common.config.MapperConfig;

import java.util.List;

/**
 * The AuthenticatedUser Mapper, mapping authority to authenticated user.
 */
@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface AppUserMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "authorities", source = "authorities")
    @Mapping(target = "roleType", source = "roleType")
    AppUser toAppUser(String userId, String email, String firstName, String lastName, List<AuthorityDTO> authorities, String roleType);

    @Mapping(target = "permissions", source = "authorityPermissions")
    AppAuthority toAppAuthority(AuthorityDTO authorityDTO);
}
