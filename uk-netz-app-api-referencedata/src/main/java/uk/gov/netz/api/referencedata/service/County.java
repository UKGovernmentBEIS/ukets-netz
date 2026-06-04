package uk.gov.netz.api.referencedata.service;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation for county name validation.
 */
@Constraint(validatedBy = County.CountyValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface County {

    String message() default "Invalid county id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * The county validator (validates against the county id)
     *
     */
    class CountyValidator implements ConstraintValidator<County, String> {

        @Autowired
        private final CountyService countyService;

        public CountyValidator() {
            this.countyService = null;
        }

        public CountyValidator(CountyService countyService) {
            this.countyService = countyService;
        }

        @Override
        public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
            if (countyService != null && !ObjectUtils.isEmpty(name)) {
                return countyService.getReferenceData().stream().anyMatch(county -> county.getName().equals(name));
            }
            return true;
        }
    }
}
