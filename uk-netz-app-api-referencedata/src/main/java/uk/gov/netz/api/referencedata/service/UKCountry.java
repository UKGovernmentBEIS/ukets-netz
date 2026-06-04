package uk.gov.netz.api.referencedata.service;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UKCountryValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UKCountry {


	String message() default "Invalid UK country name";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
