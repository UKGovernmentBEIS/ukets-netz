package uk.gov.netz.api.workflow.request.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSearchCriteria {
    
	@NotNull
	private String resourceType;
	
	@NotNull
	private String resourceId;
    
    @Builder.Default
    private Set<String> requestTypes = new HashSet<>();

    @Builder.Default
    private Set<String> requestStatuses = new HashSet<>();

    @NotNull
    private String historyCategory;
    
    @Valid
    @NotNull
    @JsonUnwrapped
    private PagingRequest paging;
    
    
}
