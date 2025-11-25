package uk.gov.netz.integration.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHolderMessage {

    private String accountHolderType;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String townOrCity;
    private String stateOrProvince;
    /*
     * Cannot accept values that are not part of the Registry’s Countries list.
     */
    private String country;
    /*
     * Mandatory if Country is UK, optional otherwise.
     */
    private String postalCode;
    private Boolean crnNotExist;
    private String companyRegistrationNumber;
    /*
     * Mandatory if the companyRegistrationNumber is null.
     */
    private String crnJustification;
}
