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
class NotBeforeCurrentDateInZoneValidatorTest {

    private static final ZoneId FURTHEST_BEHIND = ZoneId.of("Etc/GMT+12");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private final NotBeforeCurrentDateInZoneValidator validator = new NotBeforeCurrentDateInZoneValidator();

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        initializeValidator(true, "Etc/GMT+12");
    }

    @Test
    void nullValueIsValid() {
        assertTrue(validator.isValid(null, context));
    }

    @ParameterizedTest(name = "utc={0}, submitted={1}, expectedValid={2}")
    @CsvSource({
        // Furthest-behind (UTC-12) still on 24 June while UTC already on 25 June
        "2025-06-25T10:00:00Z, 2025-06-24, true",
        "2025-06-25T10:00:00Z, 2025-06-23, false",
        // Same calendar day in UTC and furthest-behind
        "2025-06-25T14:00:00Z, 2025-06-25, true",
        "2025-06-25T14:00:00Z, 2025-06-24, false"
    })
    void validatesAgainstCurrentDateInFurthestBehindZone(String utcInstant, String submittedDate, boolean expectedValid) {
        Instant instant = Instant.parse(utcInstant);
        fixClock(instant);

        LocalDate submitted = LocalDate.parse(submittedDate);
        LocalDate currentDateInConfiguredZone = instant.atZone(FURTHEST_BEHIND).toLocalDate();

        boolean actual = validator.isValid(submitted, context);

        assertEquals(
            expectedValid,
            actual,
            () -> """
                utcInstant=%s
                configuredZone=%s
                currentDateInConfiguredZone=%s
                submitted=%s
                inclusive=true
                expectedValid=%s
                """.formatted(
                utcInstant, FURTHEST_BEHIND, currentDateInConfiguredZone, submitted, expectedValid)
        );
    }

    @Test
    void usesConfiguredZone() {
        initializeValidator(true, "Asia/Tokyo");
        Instant instant = Instant.parse("2025-06-24T15:00:00Z");
        fixClock(instant);

        assertEquals(LocalDate.of(2025, 6, 25), instant.atZone(TOKYO).toLocalDate());

        assertFalse(validator.isValid(LocalDate.of(2025, 6, 24), context));
        assertTrue(validator.isValid(LocalDate.of(2025, 6, 25), context));
    }

    @ParameterizedTest(name = "inclusive={0}, currentDateValid={1}, boundaryNeighbourValid={2}")
    @CsvSource({
        "true, true, false",
        "false, false, true"
    })
    void appliesInclusiveBoundary(boolean inclusive, boolean currentDateValid, boolean boundaryNeighbourValid) {
        Instant instant = Instant.parse("2025-06-25T10:00:00Z");
        initializeValidator(inclusive, "Etc/GMT+12");
        fixClock(instant);

        LocalDate currentDate = instant.atZone(FURTHEST_BEHIND).toLocalDate();
        assertEquals(LocalDate.of(2025, 6, 24), currentDate);

        LocalDate previousDate = currentDate.minusDays(1);
        LocalDate followingDate = currentDate.plusDays(1);
        LocalDate neighbour = inclusive ? previousDate : followingDate;

        assertEquals(
            currentDateValid,
            validator.isValid(currentDate, context),
            () -> failureMessage(instant, inclusive, currentDate, currentDate, currentDateValid)
        );
        assertEquals(
            boundaryNeighbourValid,
            validator.isValid(neighbour, context),
            () -> failureMessage(instant, inclusive, currentDate, neighbour, boundaryNeighbourValid)
        );
    }

    private String failureMessage(
        Instant utcInstant,
        boolean inclusive,
        LocalDate currentDateInConfiguredZone,
        LocalDate submitted,
        boolean expectedValid
    ) {
        return """
            utcInstant=%s
            configuredZone=%s
            currentDateInConfiguredZone=%s
            submitted=%s
            inclusive=%s
            expectedValid=%s
            """.formatted(
            utcInstant, FURTHEST_BEHIND, currentDateInConfiguredZone, submitted, inclusive, expectedValid);
    }

    private void initializeValidator(boolean inclusive, String zoneId) {
        NotBeforeCurrentDateInZone annotation = mock(NotBeforeCurrentDateInZone.class);
        when(annotation.inclusive()).thenReturn(inclusive);
        when(annotation.zoneId()).thenReturn(zoneId);
        validator.initialize(annotation);
    }

    private void fixClock(Instant instant) {
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        when(context.getClockProvider()).thenReturn(() -> clock);
    }
}
