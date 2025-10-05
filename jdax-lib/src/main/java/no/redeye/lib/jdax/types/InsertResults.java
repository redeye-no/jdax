package no.redeye.lib.jdax.types;

import java.sql.RowId;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A transfer object for SQL results
 */
public record InsertResults(ResultRows records, Identities ids, int count, boolean hasIdentities) implements QueryResults {

    public InsertResults(ResultRows records) {
        this(records, null, 0, false);
    }

    public InsertResults(Identities ids) {
        this(null, ids, ids.size(), true);
    }

    public InsertResults(int count) {
        this(null, null, count, false);
    }

    /**
     * Returns identity JDBC type.
     *
     * @param index
     *
     * @return
     */
    public Integer type(int index) throws SQLException {
        return (null == ids) ? null : ids.type(index);
    }

    /**
     * Returns identity as String (using JDBC type hint if needed).
     *
     * @param index
     *
     * @return
     */
    public String stringIdentity(int index) throws SQLException {
        return (null == ids) ? null : ids.stringValue(index);
    }

    /**
     * Returns identity as long (only works for numeric-compatible types).
     *
     * @param index
     *
     * @return
     */
    public Long longIdentity(int index) throws SQLException {
        return (null == ids) ? null : ids.longValue(index);
    }

    /**
     * Returns identity as UUID (from UUID or String).
     *
     * @param index
     *
     * @return
     */
    public UUID uuidIdentity(int index) throws SQLException {
        return (null == ids) ? null : ids.uuidValue(index);
    }

    /**
     * Returns identity as RowId if supported.
     */
    public RowId rowIdIdentity(int index) throws SQLException {
        return (null == ids) ? null : ids.rowIdValue(index);
    }
}
