package uk.gov.netz.api.mireport.userdefined;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MiReportUserDefinedSearchCriteriaDTO {

    private Long categoryId;

    private String term;

    private boolean favourites;

    @Builder.Default
    private SortBy sortBy = SortBy.LAST_UPDATED_ON;

    @Builder.Default
    private Sort.Direction direction = Sort.Direction.DESC;

    @Getter
    @AllArgsConstructor
    public enum SortBy {
        REPORT_NAME("reportName"),
        LAST_UPDATED_ON("lastUpdatedOn");

        private final String column;
    }
}
