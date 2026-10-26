package uk.gov.netz.api.request.feedback.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

@UtilityClass
public class RequestFeedbackUtils {

    public boolean hasExpired(LocalDateTime now, LocalDateTime createdOn) {
        LocalDateTime expirationDate = createdOn.plusMonths(1);
        return now.isAfter(expirationDate);
    }
}
