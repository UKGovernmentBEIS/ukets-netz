package uk.gov.netz.api.user;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationTemplateName {

    public final String EMAIL_CONFIRMATION = "EmailConfirmation";
    public final String USER_ACCOUNT_CREATED = "UserAccountCreated";
    public final String USER_ACCOUNT_ACTIVATION = "UserAccountActivation";
    public final String INVITATION_TO_OPERATOR_ACCOUNT = "InvitationToOperatorAccount";
    public final String INVITATION_TO_REGULATOR_ACCOUNT = "InvitationToRegulatorAccount";
    public final String INVITATION_TO_VERIFIER_ACCOUNT = "InvitationToVerifierAccount";
    public final String INVITATION_TO_EMITTER_CONTACT = "InvitationToEmitterContact";
    public final String INVITEE_INVITATION_ACCEPTED = "InviteeInvitationAccepted";
    public final String INVITER_INVITATION_ACCEPTED = "InviterInvitationAccepted";
    public final String CHANGE_2FA = "Change2FA";
    public final String RESET_PASSWORD_REQUEST = "ResetPasswordRequest";
    public final String RESET_PASSWORD_CONFIRMATION = "ResetPasswordConfirmation";
    public final String RESET_2FA_CONFIRMATION = "Reset2FaConfirmation";

}
