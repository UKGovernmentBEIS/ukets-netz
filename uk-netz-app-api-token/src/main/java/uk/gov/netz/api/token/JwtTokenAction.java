package uk.gov.netz.api.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenAction {

    private final String subject;
    private final String claimName;

    public static final JwtTokenAction USER_REGISTRATION = new JwtTokenAction("user_registration", "user_email");
    public static final JwtTokenAction OPERATOR_INVITATION = new JwtTokenAction("operator_invitation", "authority_uuid");
    public static final JwtTokenAction REGULATOR_INVITATION = new JwtTokenAction("regulator_invitation", "authority_uuid");
    public static final JwtTokenAction VERIFIER_INVITATION = new JwtTokenAction("verifier_invitation", "authority_uuid");
    public static final JwtTokenAction CHANGE_2FA = new JwtTokenAction("change_2fa", "user_email");
    public static final JwtTokenAction GET_FILE = new JwtTokenAction("get_file", "get_file_uuid");
    public static final JwtTokenAction RESET_PASSWORD = new JwtTokenAction("reset_password", "user_email");
    
}
