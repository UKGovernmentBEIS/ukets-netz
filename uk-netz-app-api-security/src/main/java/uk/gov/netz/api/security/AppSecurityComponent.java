package uk.gov.netz.api.security;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.transform.AppUserMapper;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

import static uk.gov.netz.api.security.config.SecurityConstants.CLAIM_ROLE_TYPE;

/**
 * The AppSecurity extracting security acknowledge objects.
 */
@Component
@RequiredArgsConstructor
public class AppSecurityComponent implements AppSecurityComponentProvider {

    private final AppUserMapper userMapper;

    /**
     * Returns authorities permissions of authenticated user.
     *
     * @return List of {@link AuthorityDTO}
     */
    public AppUser getAuthenticatedUser() {
        Jwt jwt = getToken();
		if (jwt == null) {
			return null;
		}
        
        String roleType = jwt.getClaim(CLAIM_ROLE_TYPE);
        return userMapper.toAppUser(jwt.getClaimAsString(JwtClaimNames.SUB), jwt.getClaimAsString("email"), jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"), getAuthorities(roleType), roleType);
    }

    public String getAccessToken() {
    	Jwt jwt = getToken();
		if (jwt == null) {
			return null;
		}
		
        return jwt.getTokenValue();
    }

    private Jwt getToken() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null 
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
        return (Jwt) authentication.getPrincipal();
    }

    private List<AuthorityDTO> getAuthorities(String  roleType) {
        return RoleTypeConstants.OPERATOR.equals(roleType) ? getOperatorUserAuthorities() : getUserAuthorities();
    }

    private List<AuthorityDTO> getUserAuthorities() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(AuthorityDTO.class::cast)
                .toList();
    }

    private List<AuthorityDTO> getOperatorUserAuthorities() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(AuthorityDTO.class::cast)
                .filter(authority -> !ObjectUtils.isEmpty(authority.getAuthorityPermissions()))
                .toList();
    }
}
