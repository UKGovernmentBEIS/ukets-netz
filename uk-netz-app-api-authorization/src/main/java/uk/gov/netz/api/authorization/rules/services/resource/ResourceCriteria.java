package uk.gov.netz.api.authorization.rules.services.resource;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Data
@SuperBuilder
public class ResourceCriteria {
    
	@Builder.Default
    private Map<String, String> requestResources = new HashMap<>();
    
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
}
