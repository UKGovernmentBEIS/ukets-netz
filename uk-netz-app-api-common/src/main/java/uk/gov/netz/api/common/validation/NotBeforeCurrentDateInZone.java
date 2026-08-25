package uk.gov.netz.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures a {@link java.time.LocalDate} is not before the current calendar date
 * in the configured IANA zone (default: furthest behind UTC).
 * <p>
 * Use {@link #inclusive()}{@code = true} for today-or-future (Jakarta {@code @FutureOrPresent}),
 * or {@code false} for strictly after today (Jakarta {@code @Future}).
 */
@Constraint(validatedBy = NotBeforeCurrentDateInZoneValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotBeforeCurrentDateInZone {

    String message() default "must be on or after the minimum allowed date";

    boolean inclusive() default true;

    String zoneId() default "Etc/GMT+12";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
