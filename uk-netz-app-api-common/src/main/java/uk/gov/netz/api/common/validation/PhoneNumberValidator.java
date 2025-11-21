package uk.gov.netz.api.common.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.netz.api.common.domain.PhoneNumberDTO;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberValidity, PhoneNumberDTO> {

    @Override
    public boolean isValid(PhoneNumberDTO phoneNumberDTO, ConstraintValidatorContext context) {
        if (phoneNumberDTO == null ||
                (phoneNumberDTO.getCountryCode() == null && phoneNumberDTO.getNumber() == null)) {
            return true;
        }

        String countryCode = phoneNumberDTO.getCountryCode();
        String number = phoneNumberDTO.getNumber();

        if (countryCode == null || number == null) {
            return false;
        }

        return isValidUserPhoneNumber(countryCode, number);
    }

    private boolean isValidUserPhoneNumber(String countryCode, String phoneNumber) {
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = phoneUtil.parse("+" + countryCode + phoneNumber, "");
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException exception) {
            return false;
        }
    }
}