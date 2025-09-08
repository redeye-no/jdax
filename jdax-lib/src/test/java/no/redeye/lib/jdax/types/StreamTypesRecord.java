package no.redeye.lib.jdax.types;

import java.io.InputStream;
import java.io.Reader;

/**
 * This mirrors the behaviour of AllTypesRecord, but is used for
 * testing type conversion.
 */
public record StreamTypesRecord(
        int id,
        InputStream blobField,
        Reader clobField) implements VO {

    public StreamTypesRecord(
            InputStream blobField,
            Reader clobField
    ) {
        this(
                1, blobField, clobField);
    }

}
