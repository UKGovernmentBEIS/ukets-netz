package uk.gov.netz.api.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

@UtilityClass
public class DateUtils {

    public Long getDaysRemaining(LocalDate from, LocalDate to) {
        if (!ObjectUtils.isEmpty(to)) {
            from = ObjectUtils.isEmpty(from) ? LocalDate.now() : from;
            return DAYS.between(from, to);
        }
        return null;
    }

    public Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate
            .atTime(LocalTime.MIN)
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }

    public Date atEndOfDay(LocalDate date) {
        return Date.from(date
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant());
    }
}
