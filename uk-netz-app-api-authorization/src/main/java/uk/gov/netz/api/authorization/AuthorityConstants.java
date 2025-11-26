package uk.gov.netz.api.authorization;

import lombok.experimental.UtilityClass;

/**
 * Encapsulates constants related to Authorities
 */
@UtilityClass
public class AuthorityConstants {

    public static final String OPERATOR_ADMIN_ROLE_CODE = "operator_admin";
    public static final String OPERATOR_ROLE_CODE = "operator";
    public static final String VERIFIER_ADMIN_ROLE_CODE = "verifier_admin";
    public static final String CONSULTANT_AGENT = "consultant_agent";
    public static final String EMITTER_CONTACT = "emitter_contact";
    public static final String THIRD_PARTY_DATA_PROVIDER = "third_party_data_provider";
}
