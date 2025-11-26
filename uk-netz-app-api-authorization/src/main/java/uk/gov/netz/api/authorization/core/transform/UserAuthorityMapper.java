package uk.gov.netz.api.authorization.core.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityRoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthorityDTO;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface UserAuthorityMapper {

    @Mapping(target = "userId", source = "authorityRole.userId")
    @Mapping(target = "authorityCreationDate", source = "authorityRole.creationDate")
    @Mapping(target = "authorityStatus", expression = "java(hasAuthorityToEditUserAuthorities ? authorityRole.getAuthorityStatus() : null)")
    UserAuthorityDTO toUserAuthority(AuthorityRoleDTO authorityRole, boolean hasAuthorityToEditUserAuthorities);

    @Mapping(target = "userId", source = "authority.userId")
    @Mapping(target = "authorityCreationDate", source = "authority.creationDate")
    @Mapping(target = "authorityStatus", expression = "java(hasAuthorityToEditUserAuthorities ? authority.getStatus() : null)")
    UserAuthorityDTO toUserAuthority(Authority authority, boolean hasAuthorityToEditUserAuthorities);

}
