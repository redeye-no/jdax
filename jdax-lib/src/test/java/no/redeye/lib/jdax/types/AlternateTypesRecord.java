package no.redeye.lib.jdax.types;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This mirrors the behaviour of AllTypesRecord, but is used for
 * testing type conversion.
 */
public record AlternateTypesRecord(
        int id,
        int integerField,
        long bigintField,
        float realField,
        double floatField,
        double doubleField,
        //        long decimalField,
        //        int numericField,
        BigDecimal decimalField,
        BigDecimal numericField,
        LocalDate dateField,
        LocalTime timeField,
        Instant timestampField,
        String charField,
        String varcharField,
        InputStream blobField,
        Reader clobField) implements VO {

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
    public AlternateTypesRecord(
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
                realField, floatField, doubleField, decimalField, numericField,
                null, null, null, null, null, null, null);
    }

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
    public AlternateTypesRecord(
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
                realField, floatField, doubleField, BigDecimal.valueOf(decimalField), BigDecimal.valueOf(numericField),
                null, null, null, null, null, null, null);
    }

//    public AlternateTypesRecord(
//            int integerField,
//            long bigintField,
//            float realField,
//            double floatField,
//            double doubleField,
//            long decimalField, // was BigDecimal
//            double numericField, // was BigDecimal
//            LocalDate dateField,
//            LocalTime timeField,
//            Instant timestampField,
//            String charField,
//            String varcharField,
//            InputStream blobField,
//            Reader clobField) {
//        this(
//                1,
//                integerField, bigintField,
//                realField, floatField, doubleField, BigDecimal.valueOf(decimalField), BigDecimal.valueOf(numericField),
//                dateField, timeField, timestampField,
//                charField, varcharField, blobField, clobField);
//    }
}
