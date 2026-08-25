package uk.gov.netz.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures a {@link java.time.LocalDate} is not after the current calendar date
 * in the configured IANA zone (default: furthest ahead of UTC).
 */
@Constraint(validatedBy = NotAfterCurrentDateInZoneValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotAfterCurrentDateInZone {

    String message() default "must be on or before the maximum allowed date";

    int allowedFutureDays() default 0;

    String zoneId() default "Pacific/Kiritimati";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
