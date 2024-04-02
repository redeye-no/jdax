package no.redeye.lib.jdax.types;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class EmptyStream extends InputStream {
    @Override
    public int read() throws IOException {
        return -1; // Indicate end of stream
    }
}