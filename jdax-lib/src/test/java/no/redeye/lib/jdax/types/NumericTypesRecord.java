package no.redeye.lib.jdax.types;

import java.math.BigDecimal;

/**
 * This mirrors the behaviour of AllTypesRecord, but is used for
 * testing type conversion.
 */
public record NumericTypesRecord(
        int id,
        int integerField,
        long bigintField,
        float realField,
        double floatField,
        double doubleField,
        BigDecimal decimalField,
        BigDecimal numericField) implements VO {

    public NumericTypesRecord(
            int integerField,
            long bigintField,
            float realField,
            double floatField,
            double doubleField,
            BigDecimal decimalField,
            BigDecimal numericField
    ) {
        this(
                1,
                integerField, bigintField,
                realField, floatField, doubleField, decimalField, numericField);
    }

    public NumericTypesRecord(
            int integerField,
            long bigintField,
            float realField,
            double floatField,
            double doubleField,
            long decimalField, // was BigDecimal
            double numericField // was BigDecimal) {
    ) {
        this(
                1,
                integerField, bigintField,
                realField, floatField, doubleField, BigDecimal.valueOf(decimalField), BigDecimal.valueOf(numericField));
    }

}
