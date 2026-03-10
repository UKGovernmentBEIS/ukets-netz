package uk.gov.netz.api.verificationbody.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Embeddable
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address {

    /** The line 1 address. */
    @Column(name = "line1")
    @NotBlank
    private String line1;

    /** The line 2 address. */
    @Column(name = "line2")
    private String line2;

    /** The city. */
    @Column(name = "city")
    @NotBlank
    private String city;

    /** The country. */
    @Column(name = "country")
    @NotBlank
    private String country;

    @Column(name = "postcode")
    @NotBlank
    private String postcode;
}
