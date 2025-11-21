package uk.gov.netz.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation for PhoneNumber integrity validation.
 */
@Constraint(validatedBy = PhoneNumberIntegrityValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PhoneNumberIntegrity {

    String message() default "Country code or phone number missing";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
