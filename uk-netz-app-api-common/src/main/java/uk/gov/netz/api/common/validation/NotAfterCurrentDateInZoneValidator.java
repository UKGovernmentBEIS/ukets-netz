package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class NotAfterCurrentDateInZoneValidator implements ConstraintValidator<NotAfterCurrentDateInZone, LocalDate> {

    private int allowedFutureDays;
    private ZoneId zoneId;

    @Override
    public void initialize(NotAfterCurrentDateInZone constraintAnnotation) {
        this.allowedFutureDays = constraintAnnotation.allowedFutureDays();
        this.zoneId = ZoneId.of(constraintAnnotation.zoneId());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Clock clock = context.getClockProvider().getClock();
        LocalDate todayInConfiguredZone = LocalDate.now(clock.withZone(zoneId));
        return !value.isAfter(todayInConfiguredZone.plusDays(allowedFutureDays));
    }
}
