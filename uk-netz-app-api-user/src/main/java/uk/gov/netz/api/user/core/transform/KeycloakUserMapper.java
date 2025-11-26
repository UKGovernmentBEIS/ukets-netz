package uk.gov.netz.api.user.core.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.user.core.domain.model.keycloak.KeycloakUserInfo;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface KeycloakUserMapper {

    UserInfo toUserInfo(KeycloakUserInfo keycloakUserInfo);
}
