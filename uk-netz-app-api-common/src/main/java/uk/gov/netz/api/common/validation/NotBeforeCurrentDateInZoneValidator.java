package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class NotBeforeCurrentDateInZoneValidator implements ConstraintValidator<NotBeforeCurrentDateInZone, LocalDate> {

    private boolean inclusive;
    private ZoneId zoneId;

    @Override
    public void initialize(NotBeforeCurrentDateInZone constraintAnnotation) {
        this.inclusive = constraintAnnotation.inclusive();
        this.zoneId = ZoneId.of(constraintAnnotation.zoneId());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Clock clock = context.getClockProvider().getClock();
        LocalDate todayInConfiguredZone = LocalDate.now(clock.withZone(zoneId));

        if (inclusive) {
            return !value.isBefore(todayInConfiguredZone);
        }
        return value.isAfter(todayInConfiguredZone);
    }
}
