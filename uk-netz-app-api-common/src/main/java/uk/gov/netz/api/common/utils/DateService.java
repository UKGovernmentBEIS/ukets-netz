package uk.gov.netz.api.common.utils;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

/*
This is not a Utility class in order to be able to mock it
 */
@Service
public class DateService {

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }

    public LocalDate getLocalDate() {
        return LocalDate.now();
    }

    public Year getYear() {
        return Year.now();
    }
}
