package no.redeye.lib.jdax.types;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This mirrors the behaviour of AllTypesRecord, but is used for
 * testing type conversion.
 */
public record TemporalTypesRecord(
        int id,
        LocalDate dateField,
        LocalTime timeField,
        Instant timestampField) implements VO {

    /**
     * Numeric values only
     *
     * @param integerField
     * @param bigintField
     * @param realField
     * @param floatField
     * @param doubleField
     * @param decimalField
     * @param numericField
     */
    public TemporalTypesRecord(
            LocalDate dateField,
            LocalTime timeField,
            Instant timestampField
    ) {
        this(
                1,
                dateField, timeField, timestampField);
    }
}
