package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.common.domain.PhoneNumberDTO;
import uk.gov.netz.api.common.validation.PhoneNumberValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        validator = new PhoneNumberValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(null); // Assuming PhoneNumberValidity doesn't have any settings to initialize
    }

    @Test
    void testNullPhoneNumberDTO() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testNullCountryCode() {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO(null, "1234567890");
        assertFalse(validator.isValid(phoneNumberDTO, context));
    }

    @Test
    void testNullNumber() {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO("+1", null);
        assertFalse(validator.isValid(phoneNumberDTO, context));
    }

    @Test
    void testValidPhoneNumber() {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO("+30", "6946666665");
        assertTrue(validator.isValid(phoneNumberDTO, context)); // Adjust according to a valid number for your scenario
    }

    @Test
    void testInvalidPhoneNumber() {
        PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO("+1", "abc123");
        assertFalse(validator.isValid(phoneNumberDTO, context));
    }
}
