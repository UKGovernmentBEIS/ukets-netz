package uk.gov.netz.api.common.validation.uniqueelements;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniqueFieldModel {

    @NotBlank
    @Size(max=10000)
    private String manufacturer;

    @NotBlank
    @Size(max=10000)
    private String model;

    @NotBlank
    @Size(max=10000)
    @UniqueField
    private String location;

    @NotBlank
    @Size(max=10000)
    @UniqueField
    private String city;
}
