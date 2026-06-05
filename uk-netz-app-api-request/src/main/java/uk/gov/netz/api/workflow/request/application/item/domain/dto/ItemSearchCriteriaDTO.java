package uk.gov.netz.api.workflow.request.application.item.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
