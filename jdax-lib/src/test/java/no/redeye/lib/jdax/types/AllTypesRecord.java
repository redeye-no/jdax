package no.redeye.lib.jdax.types;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 */
public record AllTypesRecord(
        int id,
        int integerField,
        long bigintField,
        float realField,
        double floatField,
        double doubleField,
        BigDecimal decimalField,
        BigDecimal numericField,
        LocalDate dateField,
        LocalTime timeField,
        Instant timestampField,
        String charField,
        String varcharField,
        InputStream blobField,
        Reader clobField) implements VO {

    public AllTypesRecord(
            int integerField,
            long bigintField,
            float realField,
            double floatField,
            double doubleField,
            BigDecimal decimalField,
            BigDecimal numericField,
            LocalDate dateField,
            LocalTime timeField,
            Instant timestampField,
            String charField,
            String varcharField,
            InputStream blobField,
            Reader clobField) {
        this(
                1,
                integerField, bigintField,
                realField, floatField, doubleField, decimalField, numericField,
                dateField, timeField, timestampField,
                charField, varcharField, blobField, clobField);
    }

}
