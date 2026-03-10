package uk.gov.netz.api.verificationbody.domain.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

public class StatusPendingValidator implements ConstraintValidator<StatusPending, VerificationBodyStatus> {

    @Override
    public boolean isValid(VerificationBodyStatus status, ConstraintValidatorContext context) {
        return !VerificationBodyStatus.PENDING.equals(status);
    }
}
