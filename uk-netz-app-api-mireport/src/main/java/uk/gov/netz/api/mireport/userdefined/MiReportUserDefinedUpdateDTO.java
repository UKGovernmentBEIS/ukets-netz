package uk.gov.netz.api.mireport.userdefined;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class MiReportUserDefinedUpdateDTO {

    @Valid
    @NotNull
    private MiReportUserDefinedDTO userDefinedDTO;

    @NotBlank
    @Size(max = 10000)
    private String reasonForChange;

}
