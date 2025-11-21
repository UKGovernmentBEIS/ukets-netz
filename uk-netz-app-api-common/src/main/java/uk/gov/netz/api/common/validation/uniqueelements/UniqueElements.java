package uk.gov.netz.api.common.validation.uniqueelements;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE_USE, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueElementsValidator.class)
@Documented
public @interface UniqueElements {

    String message() default "UniqueElementsValidationError";

    Class<?>[] groups() default { };
    
    Class<? extends Payload>[] payload() default { };
}
