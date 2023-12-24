package no.redeye.lib.jdax.types;

import java.util.List;

/**
 * A transfer object for SQL results
 */
public record QueryResults(ResultRows records, List<Long> ids, int count) {

    public QueryResults(ResultRows records) {
        this(records, null, 0);
    }

    public QueryResults(List<Long> ids, int z) {
        this(null, ids, 0);
    }

    public QueryResults(int count) {
        this(null, null, count);
    }
}
