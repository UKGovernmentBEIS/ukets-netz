package uk.gov.netz.api.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotAfterCurrentDateInZoneValidatorTest {

    private static final ZoneId FURTHEST_AHEAD = ZoneId.of("Pacific/Kiritimati");
    private static final ZoneId LONDON = ZoneId.of("Europe/London");

    private final NotAfterCurrentDateInZoneValidator validator = new NotAfterCurrentDateInZoneValidator();

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        initializeValidator(0, "Pacific/Kiritimati");
    }

    @Test
    void nullValueIsValid() {
        assertTrue(validator.isValid(null, context));
    }

    @ParameterizedTest(name = "utc={0}, submitted={1}, expectedValid={2}")
    @CsvSource({
        // Kiritimati (UTC+14) already on 25 June while UTC still on 24 June
        "2025-06-24T11:00:00Z, 2025-06-25, true",
        "2025-06-24T11:00:00Z, 2025-06-26, false",
        // Same calendar day in UTC and Kiritimati
        "2025-06-25T10:00:00Z, 2025-06-26, true",
        "2025-06-25T10:00:00Z, 2025-06-27, false"
    })
    void validatesAgainstCurrentDateInFurthestAheadZone(String utcInstant, String submittedDate, boolean expectedValid) {
        Instant instant = Instant.parse(utcInstant);
        fixClock(instant);

        LocalDate submitted = LocalDate.parse(submittedDate);
        LocalDate currentDateInConfiguredZone = instant.atZone(FURTHEST_AHEAD).toLocalDate();

        boolean actual = validator.isValid(submitted, context);

        assertEquals(
            expectedValid,
            actual,
            () -> """
                utcInstant=%s
                configuredZone=%s
                currentDateInConfiguredZone=%s
                submitted=%s
                allowedFutureDays=0
                expectedValid=%s
                """.formatted(
                utcInstant, FURTHEST_AHEAD, currentDateInConfiguredZone, submitted, expectedValid)
        );
    }

    @Test
    void usesConfiguredZone() {
        initializeValidator(0, "Europe/London");
        Instant instant = Instant.parse("2025-06-24T22:00:00Z");
        fixClock(instant);

        assertEquals(LocalDate.of(2025, 6, 24), instant.atZone(LONDON).toLocalDate());

        assertTrue(validator.isValid(LocalDate.of(2025, 6, 24), context));
        assertFalse(validator.isValid(LocalDate.of(2025, 6, 25), context));
    }

    @ParameterizedTest(name = "allowedFutureDays={0}, exactMax={1}, dayAfter={2}")
    @CsvSource({
        "0, 2025-06-26, 2025-06-27",
        "1, 2025-06-27, 2025-06-28",
        "5, 2025-07-01, 2025-07-02"
    })
    void appliesAllowedFutureDays(int allowedFutureDays, String exactMaximumDate, String dayAfterMaximum) {
        Instant instant = Instant.parse("2025-06-25T10:00:00Z");
        initializeValidator(allowedFutureDays, "Pacific/Kiritimati");
        fixClock(instant);

        LocalDate currentDateInConfiguredZone = instant.atZone(FURTHEST_AHEAD).toLocalDate();
        assertEquals(LocalDate.of(2025, 6, 26), currentDateInConfiguredZone);

        LocalDate exactMax = LocalDate.parse(exactMaximumDate);
        LocalDate dayAfter = LocalDate.parse(dayAfterMaximum);

        assertTrue(
            validator.isValid(exactMax, context),
            () -> failureMessage(instant, allowedFutureDays, currentDateInConfiguredZone, exactMax, true)
        );
        assertFalse(
            validator.isValid(dayAfter, context),
            () -> failureMessage(instant, allowedFutureDays, currentDateInConfiguredZone, dayAfter, false)
        );
    }

    private String failureMessage(
        Instant utcInstant,
        int allowedFutureDays,
        LocalDate currentDateInConfiguredZone,
        LocalDate submitted,
        boolean expectedValid
    ) {
        return """
            utcInstant=%s
            configuredZone=%s
            currentDateInConfiguredZone=%s
            submitted=%s
            allowedFutureDays=%s
            expectedValid=%s
            """.formatted(
            utcInstant, FURTHEST_AHEAD, currentDateInConfiguredZone, submitted, allowedFutureDays, expectedValid);
    }

    private void initializeValidator(int allowedFutureDays, String zoneId) {
        NotAfterCurrentDateInZone annotation = mock(NotAfterCurrentDateInZone.class);
        when(annotation.allowedFutureDays()).thenReturn(allowedFutureDays);
        when(annotation.zoneId()).thenReturn(zoneId);
        validator.initialize(annotation);
    }

    private void fixClock(Instant instant) {
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        when(context.getClockProvider()).thenReturn(() -> clock);
    }
}
