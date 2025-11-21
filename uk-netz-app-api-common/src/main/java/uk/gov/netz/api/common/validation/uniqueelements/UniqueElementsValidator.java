package uk.gov.netz.api.common.validation.uniqueelements;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import java.security.NoSuchAlgorithmException;
import java.util.Set;


@Component
public class UniqueElementsValidator
        implements ConstraintValidator<UniqueElements, Set<?>> {

    @Override
    public boolean isValid(Set<?> o, ConstraintValidatorContext constraintValidatorContext) {
        try {

            UniqueElementsUtilsValidateSetResult result = UniqueElementsUtils.validateSet(o);

            constraintValidatorContext.disableDefaultConstraintViolation();

            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                             "Unique elements constraint violation")
                    .addConstraintViolation();

            return result.getResult();

        } catch (IllegalAccessException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
