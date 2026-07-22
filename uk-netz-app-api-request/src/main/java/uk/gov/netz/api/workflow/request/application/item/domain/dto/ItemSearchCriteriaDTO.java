package uk.gov.netz.api.workflow.request.application.item.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemOrderBy;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSearchCriteriaDTO {

    @Builder.Default
    private ItemOrderBy orderBy = ItemOrderBy.NEWEST_FIRST;

    @Size(max = 255)
    private String requestType;

    @Size(max = 255)
    private String searchTerm;

    @JsonIgnore
    public boolean hasNoFilters() {
        return StringUtils.isBlank(searchTerm) && StringUtils.isBlank(requestType);
    }
}
