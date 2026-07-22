package uk.gov.netz.api.common.config;

import jakarta.persistence.AttributeConverter;

import java.time.Year;

public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Year year) {
        return year != null ? year.getValue() : null;
    }

    @Override
    public Year convertToEntityAttribute(Integer value) {
        return value != null ? Year.of(value) : null;
    }
}
