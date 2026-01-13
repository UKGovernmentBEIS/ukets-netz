package uk.gov.netz.integration.model.metscontacts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetsContactsMessage {

    private String firstName;
    private String lastName;
    private String telephoneCountryCode;
    private String telephoneNumber;
    private String mobilePhoneCountryCode;
    private String mobileNumber;
    private String email;
    private String userType;
    private List<String> contactTypes;

}