package uk.gov.netz.api.security;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;

import java.util.Collections;
import java.util.Map;

import static uk.gov.netz.api.security.config.SecurityConstants.CLAIM_ROLE_TYPE;


@RequiredArgsConstructor
public class RoleTypeClaimConverter implements Converter<Map<String, Object>, Map<String, Object>> {
    private final UserRoleTypeService userRoleTypeService;

    private final MappedJwtClaimSetConverter delegate =
            MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

    public Map<String, Object> convert(@NotNull Map<String, Object> claims) {
        Map<String, Object> convertedClaims = this.delegate.convert(claims);
        convertedClaims.put(CLAIM_ROLE_TYPE, getRoleType((String)claims.get(JwtClaimNames.SUB)));
        return convertedClaims;
    }

    private String getRoleType(String userId) {
		return userRoleTypeService.getUserRoleTypeByUserIdOpt(userId).map(UserRoleTypeDTO::getRoleType).orElse(null);
    }
}
