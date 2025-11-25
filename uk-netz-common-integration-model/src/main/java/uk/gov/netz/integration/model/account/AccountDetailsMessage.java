package uk.gov.netz.integration.model.account;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsMessage {

    private String emitterId;
    private String accountName;
    /*
     * Only following values are accepted: EA, SEPA, NRW, OPRED, DAERA
     */
    private String regulator;
    /*
     * First year of verified emission submission.
     * Cannot be less than 2021 for OAH/AOHA
     * Cannot be less than 2026 for MOHA
     */
    private Integer firstYearOfVerifiedEmissions;
    /*
     * Applicable only for installation
     */
    private Set<String> installationActivityTypes;
    /*
     * Applicable only for installation
     */
    private String installationName;
    /*
     * Applicable only for installation
     */
    private String permitId;
    /*
     * Applicable for aviation and maritime
     * In case of maritime and if Monitoring Plan ID is not available, Registry should save the Emitter ID.
     */
    private String monitoringPlanId;
    /*
     * Applicable only for maritime
     */
    private String companyImoNumber;
}
