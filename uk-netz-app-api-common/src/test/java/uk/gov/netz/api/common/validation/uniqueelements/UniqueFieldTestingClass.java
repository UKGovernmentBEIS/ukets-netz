package uk.gov.netz.api.common.validation.uniqueelements;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniqueFieldTestingClass {

    @NotNull
    @Valid
    @UniqueField
    private UniqueFieldModel uniqueField;

    @Size(max = 10000)
    private String subtype;


}
