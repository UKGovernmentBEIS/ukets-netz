package uk.gov.netz.api.authorization.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.springframework.security.core.GrantedAuthority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityDTO implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

	private String code;

    private AuthorityStatus status;

    private Long accountId;

    private CompetentAuthorityEnum competentAuthority;

    private Long verificationBodyId;
    
    private Long thirdPartyDataProviderId;

    @Builder.Default
    private List<String> authorityPermissions = new ArrayList<>();

    @Override
    public String getAuthority() {
        return getCode();
    }

}
