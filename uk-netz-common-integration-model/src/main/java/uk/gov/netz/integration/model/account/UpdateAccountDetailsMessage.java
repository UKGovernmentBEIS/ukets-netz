package uk.gov.netz.integration.model.account;

import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDetailsMessage {

    private Set<String> installationActivityTypes = new HashSet<>();
    private String service;
    private String registryId;

    //Common properties with the create account
    private String installationName;
    private String accountName;
    private String permitId;
    private String monitoringPlanId;
    private String companyImoNumber;
    /*
     * First year of verified emission submission.
     * Cannot be less than 2021 for OAH/AOHA
     * Cannot be less than 2026 for MOHA
     */
    private Integer firstYearOfVerifiedEmissions;
}
