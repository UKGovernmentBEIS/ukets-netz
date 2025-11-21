package uk.gov.netz.api.common.domain;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.common.validation.CountryCode;

/**
 * The phone number details DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberDTO {

    /** The country code phone. */
    @CountryCode(message = "{phoneNumber.countryCode.typeMismatch}")
    private String countryCode;

    /** The phone number. */
    @Size(max = 255, message = "{phoneNumber.number.typeMismatch}")
    private String number;
}
