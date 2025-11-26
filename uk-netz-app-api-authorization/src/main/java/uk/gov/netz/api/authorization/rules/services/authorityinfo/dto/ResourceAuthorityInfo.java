package uk.gov.netz.api.authorization.rules.services.authorityinfo.dto;

import java.util.Map;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceAuthorityInfo {

	@Builder.Default
    private Map<String, String> requestResources = new HashMap<>();
	
	public Long getAccountId() {
        return requestResources.get(ResourceType.ACCOUNT) != null 
        		? Long.parseLong(requestResources.get(ResourceType.ACCOUNT))
        				: null;
    }
    
}
