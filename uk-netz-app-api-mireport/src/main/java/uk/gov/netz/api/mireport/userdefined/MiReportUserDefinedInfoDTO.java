package uk.gov.netz.api.mireport.userdefined;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MiReportUserDefinedInfoDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String reportName;

    @Builder.Default
    private List<MiReportUserDefinedCategoryDTO> categories = new ArrayList<>();

    @Size(max = 10000)
    private String description;
}
