package uk.gov.netz.api.mireport.userdefined;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String queryDefinition;
}
