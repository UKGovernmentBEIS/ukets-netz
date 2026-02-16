package uk.gov.netz.api.mireport.system.accountsregulatorsitecontacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountAssignedRegulatorSiteContact {

    @JsonProperty(value = "Account ID")
    private String accountId;

    @JsonProperty(value = "Account name")
    private String accountName;

    @JsonProperty(value = "Account status")
    private String accountStatus;

    @JsonProperty(value = "User status")
    private String authorityStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonProperty(value = "Assigned regulator")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assignedRegulatorName;

    public static List<String> getColumnNames() {
        return List.of("Account ID", "Account name", "Account status",
                "User status", "Assigned regulator");
    }
}
