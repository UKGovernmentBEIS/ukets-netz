package uk.gov.netz.api.account.domain.dto;

import org.springframework.data.domain.Sort.Direction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.common.domain.PagingRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSearchCriteria {
    
    private String term;
    
    @Valid
    @NotNull
    private PagingRequest paging;
    
    private SortBy sortBy;
    
    private Direction direction;
    
    @Getter
    @AllArgsConstructor
    public static enum SortBy {
    	ACCOUNT_ID("id"),
    	ACCOUNT_BUSINESS_ID("businessId")
    	;
    	
    	private final String entityProperty;
    }
}