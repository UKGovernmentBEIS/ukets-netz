package uk.gov.netz.api.account.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteContactSearchCriteriaDTO {

    @Size(max = 255)
    private String businessId;

    @JsonIgnore
    public boolean hasNoFilters() {
        return StringUtils.isBlank(businessId);
    }
}
