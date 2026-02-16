package uk.gov.netz.api.mireport.system.accountuserscontacts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
public class AccountUserContact {
    private String userId;

    @JsonProperty(value = "Account ID")
    private String accountId;

    @JsonProperty(value = "Account name")
    private String accountName;

    @JsonProperty(value = "Account status")
    private String accountStatus;

    @JsonProperty(value = "Is User Primary contact?")
    private Boolean primaryContact;

    @JsonProperty(value = "User status")
    private String authorityStatus;

    @JsonProperty(value = "Name")
    @JsonInclude(Include.NON_NULL)
    private String name;

    @JsonProperty(value = "Telephone")
    @JsonInclude(Include.NON_NULL)
    private String telephone;

    @JsonProperty(value = "Last logon")
    @JsonInclude(Include.NON_NULL)
    private String lastLogon;

    @JsonProperty(value = "Email")
    @JsonInclude(Include.NON_NULL)
    private String email;

    @JsonProperty(value = "User role")
    private String role;

    public static List<String> getColumnNames() {
        return List.of("Account ID", "Account name", "Account status",
                "Is User Primary contact?",
                "User status", "Name", "Telephone", "Last logon", "Email", "User role");
    }
}
