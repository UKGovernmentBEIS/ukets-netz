package uk.gov.netz.api.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DateUtilsTest {
    private static final LocalDate NOW = LocalDate.now();

    @ParameterizedTest
    @MethodSource("getDaysRemainingScenarios")
    void getDaysRemaining(LocalDate pauseDate, LocalDate dueDate, Long expected) {
        assertThat(DateUtils.getDaysRemaining(pauseDate, dueDate)).isEqualTo(expected);
    }

    private static Stream<Arguments> getDaysRemainingScenarios() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(null, NOW, 0L),
            Arguments.of(NOW, NOW, 0L),
            Arguments.of(NOW.plusDays(1), NOW, -1L),
            Arguments.of(NOW, NOW.plusDays(1), 1L)
        );
    }

    @Test
    void convertLocalDateToDate() {
        ZonedDateTime actualDate = ZonedDateTime.ofInstant(
            DateUtils.convertLocalDateToDate(NOW).toInstant(), ZoneId.systemDefault());

        assertDay(actualDate, 0, 0, 0);
    }

    @Test
    void atEndOfDay() {
        ZonedDateTime actualDate = ZonedDateTime.ofInstant(
            DateUtils.atEndOfDay(NOW).toInstant(), ZoneId.systemDefault());

        assertDay(actualDate, 23, 59, 59);
    }

    private static void assertDay(ZonedDateTime actualDate, int hour, int minute, int second) {
        assertThat(actualDate.getYear()).isEqualTo(NOW.getYear());
        assertThat(actualDate.getMonth()).isEqualTo(NOW.getMonth());
        assertThat(actualDate.getDayOfMonth()).isEqualTo(NOW.getDayOfMonth());
        assertThat(actualDate.getHour()).isEqualTo(hour);
        assertThat(actualDate.getMinute()).isEqualTo(minute);
        assertThat(actualDate.getSecond()).isEqualTo(second);
    }
}