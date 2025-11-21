package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

    public static void addConstraintViolation(ConstraintValidatorContext context, String messageTemplate, String fieldName) {
        context.buildConstraintViolationWithTemplate(messageTemplate)
            .addPropertyNode(fieldName).addBeanNode().addConstraintViolation();
    }

}
