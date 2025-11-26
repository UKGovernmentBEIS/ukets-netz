package uk.gov.netz.api.workflow.request.flow.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.RequestMetadata;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestParams {

    private String type;
    private LocalDateTime creationDate;
    private RequestPayload requestPayload;
    private RequestMetadata requestMetadata;
    @Builder.Default
    private Map<String, String> requestResources = new HashMap<>();
    @Builder.Default
    private Map<String, Object> processVars = new HashMap<>();

    @With
    private String requestId;
    
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

}
