package uk.gov.netz.api.user.core.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KeycloakUserAttributes {

    JOB_TITLE("jobTitle"),
    LINE_1("line1"),
    LINE_2("line2"),
    CITY("city"),
    COUNTY("county"),
    POSTCODE("postcode"),
    PHONE_NUMBER_CODE("phoneNumberCode"),
    PHONE_NUMBER("phoneNumber"),
    MOBILE_NUMBER_CODE("mobileNumberCode"),
    MOBILE_NUMBER("mobileNumber"),
    ;

    /** The name. */
    private final String name;
}
