package uk.gov.netz.api.mireport.userdefined.category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiReportUserDefinedCategoryDTO {

    @NotNull
    private Long id;
    private String name;

}
