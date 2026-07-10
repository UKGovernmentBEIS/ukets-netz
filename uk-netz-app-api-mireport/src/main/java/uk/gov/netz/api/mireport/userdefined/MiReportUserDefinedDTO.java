package uk.gov.netz.api.mireport.userdefined;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;
import uk.gov.netz.api.mireport.userdefined.custom.ValidSqlQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MiReportUserDefinedDTO {

    @NotNull
    @Size(max = 255)
    private String reportName;

    @Size(max = 10000)
    private String description;

    @Size(max = 10000)
    @NotNull
    @ValidSqlQuery
    private String queryDefinition;

    @Valid
    @Builder.Default
    private Set<MiReportUserDefinedCategoryDTO> categories = new HashSet<>();

    private LocalDateTime lastUpdatedOn;
}
