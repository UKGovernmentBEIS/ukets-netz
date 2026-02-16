package uk.gov.netz.api.mireport.userdefined.custom;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Data
@SuperBuilder
public class CustomMiReportQuery {
    @NotEmpty
    private String sqlQuery;
}
