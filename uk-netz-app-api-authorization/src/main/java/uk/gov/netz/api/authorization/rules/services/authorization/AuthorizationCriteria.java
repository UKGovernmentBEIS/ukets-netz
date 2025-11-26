package uk.gov.netz.api.authorization.rules.services.authorization;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Data
@AllArgsConstructor
@SuperBuilder
public class AuthorizationCriteria {
	
	@Builder.Default
    private Map<String, String> requestResources = new HashMap<>();
    private String permission;
    
    public Long getAccountId() {
        return requestResources.get(ResourceType.ACCOUNT) != null 
        		? Long.parseLong(requestResources.get(ResourceType.ACCOUNT))
        				: null;
    }
    
    public CompetentAuthorityEnum getCompetentAuthority() {
        return requestResources.get(ResourceType.CA) != null 
        		? CompetentAuthorityEnum.valueOf(requestResources.get(ResourceType.CA))
        				: null;
    }
    
    public Long getVerificationBodyId() {
        return requestResources.get(ResourceType.VERIFICATION_BODY) != null 
        		? Long.parseLong(requestResources.get(ResourceType.VERIFICATION_BODY))
        				: null;
    }
    
    public Long getThirdPartyDataProviderId() {
        return requestResources.get(ResourceType.THIRD_PARTY_DATA_PROVIDER) != null 
        		? Long.parseLong(requestResources.get(ResourceType.THIRD_PARTY_DATA_PROVIDER))
        				: null;
    }
}
