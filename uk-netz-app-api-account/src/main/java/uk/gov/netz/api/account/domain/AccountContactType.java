package uk.gov.netz.api.account.domain;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class AccountContactType {

    public final String PRIMARY = "PRIMARY";
    public final String SECONDARY = "SECONDARY";
    public final String SERVICE = "SERVICE";
    public final String FINANCIAL = "FINANCIAL";
    public final String CA_SITE = "CA_SITE";
    public final String VB_SITE = "VB_SITE";

        public static Set<String> getOperatorAccountContactTypes() {
        return Set.of(PRIMARY, SECONDARY, FINANCIAL, SERVICE);
    }
    
}
