package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.common.domain.PhoneNumberDTO;

/**
 * The PhoneNumber validation that validates if country code or number are blank.
 */
public class PhoneNumberNotBlankValidator implements ConstraintValidator<PhoneNumberNotBlank, PhoneNumberDTO> {
    /** {@inheritDoc} */
    @Override
    public boolean isValid(PhoneNumberDTO phoneNumberDTO, ConstraintValidatorContext constraintValidatorContext) {
        return phoneNumberDTO != null && !ObjectUtils.isEmpty(phoneNumberDTO.getCountryCode()) && !ObjectUtils.isEmpty(phoneNumberDTO.getCountryCode().trim())
                && !ObjectUtils.isEmpty(phoneNumberDTO.getNumber()) && !ObjectUtils.isEmpty(phoneNumberDTO.getNumber().trim());
    }
}
