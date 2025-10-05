package no.redeye.lib.jdax.types;

import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Identities {

    private final List<Object> rawPairs; // alternating [type, value]

    public Identities(List<Object> rawPairs) {
        if (rawPairs.size() % 2 != 0) {
            throw new IllegalArgumentException("List must contain (type, value) pairs");
        }
        this.rawPairs = new ArrayList<>(rawPairs);
    }

    /**
     * Number of identities stored.
     *
     * @return
     */
    public int size() {
        return rawPairs.size() / 2;
    }

    /**
     * Get the raw JDBC type (java.sql.Types) of the identity at index.
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public int type(int index) throws SQLException {
        if ((index * 2) < rawPairs.size()) {
            return (Integer) rawPairs.get(index * 2);
        }
        throw new SQLException("No identity type at index " + index);
    }

    /**
     * Get the raw object of the identity at index.
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public Object raw(int index) throws SQLException {
        if ((index * 2 + 1) < rawPairs.size()) {
            return rawPairs.get(index * 2 + 1);
        }
        throw new SQLException("No identity value at index " + index);
    }

    /**
     * Returns identity as String (using JDBC type hint if needed).
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public String stringValue(int index) throws SQLException {
        Object v = raw(index);
        if (v == null) {
            return null;
        }
        return v.toString();
    }

    /**
     * Returns identity as long (only works for numeric-compatible types).
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public Long longValue(int index) throws SQLException {
        Object v = raw(index);
        if (v == null) {
            return null;
        }
        if (v instanceof Number num) {
            return num.longValue();
        }
        int t = type(index);
        if (t == Types.CHAR || t == Types.VARCHAR) {
            return Long.valueOf(v.toString());
        }
        throw new IllegalStateException("Identity at index " + index
                + " is not numeric (type=" + t + ", value=" + v + ")");
    }

    /**
     * Returns identity as UUID (from UUID or String).
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public UUID uuidValue(int index) throws SQLException {
        Object v = raw(index);
        if (v == null) {
            return null;
        }
        if (v instanceof UUID uuid) {
            return uuid;
        }
        if (v instanceof String s) {
            return UUID.fromString(s);
        }
        throw new IllegalStateException("Identity at index " + index
                + " is not a UUID (type=" + type(index) + ", value=" + v + ")");
    }

    /**
     * Returns identity as RowId if supported.
     *
     * @param index
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public RowId rowIdValue(int index) throws SQLException {
        Object v = raw(index);
        if (v == null) {
            return null;
        }
        if (v instanceof RowId rowId) {
            return rowId;
        }
        throw new IllegalStateException("Identity at index " + index
                + " is not a RowId (type=" + type(index) + ", value=" + v + ")");
    }
}
