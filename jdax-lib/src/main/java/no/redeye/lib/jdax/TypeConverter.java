package no.redeye.lib.jdax;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import no.redeye.lib.jdax.types.EmptyStream;

/**
 *
 */
public class TypeConverter {

    /**
     * Return a resultSet value of the specified return type.
     *
     * @param rs
     * @param columnIndex
     * @param columnType
     * @param allowNulls
     * @param returnType
     *
     * @return
     *
     * @throws SQLException
     */
    public static Object getValueForType(ResultSet rs, int columnIndex, int columnType, boolean allowNulls, Class<?> returnType) throws SQLException {
        Object value;
        switch (columnType) {
            case Types.VARCHAR, Types.LONGNVARCHAR -> {
                String v = rs.getString(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v : "";
            }
            case Types.CHAR -> {
                String v = rs.getString(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }

                String s = (null != v) ? v : "";
                int length = s.length();
                if (length > 0) {
                    v = String.format("%-" + length + "s", s).substring(0, length);
                    value = v.chars() // IntStream of code points
                            .mapToObj(c -> (char) c) // convert to Character
                            .toArray(Character[]::new);
                } else {
                    value = new Character[0];
                }
            }
            case Types.DECIMAL, Types.NUMERIC -> {
                BigDecimal v = rs.getBigDecimal(columnIndex);

                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v : BigDecimal.ZERO;
            }
            case Types.BIT, Types.BOOLEAN -> {
                Boolean v = rs.getBoolean(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Boolean.FALSE) : v;
            }
            case Types.SMALLINT -> {
                Short v = rs.getShort(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Short.valueOf((short) 0)) : v;
            }
            case Types.INTEGER -> {
                Integer v = rs.getInt(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Integer.valueOf(0)) : v;
            }
            case Types.TINYINT -> {
                Byte v = rs.getByte(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Byte.valueOf((byte) 0)) : v;
            }
            case Types.BIGINT -> {
                Long v = rs.getLong(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Long.valueOf(0L)) : v;
            }
            case Types.DOUBLE, Types.FLOAT -> {
                Double v = rs.getDouble(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Double.valueOf(0.0)) : v;
            }
            case Types.REAL -> {
                Float v = rs.getFloat(columnIndex);
                value = rs.wasNull() ? (allowNulls ? null : Float.valueOf(0.0f)) : v;
            }
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
                byte[] v = rs.getBytes(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v : new byte[0];
            }
            case Types.DATE -> {
                java.sql.Date v = rs.getDate(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v.toLocalDate() : LocalDate.EPOCH;
            }
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> {
                Time v = rs.getTime(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v.toLocalTime() : LocalTime.MIDNIGHT;
            }
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
                Timestamp v = rs.getTimestamp(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v.toInstant() : Instant.EPOCH;
            }
            case Types.BLOB -> {
                Blob v = rs.getBlob(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v.getBinaryStream() : new EmptyStream();
            }
            case Types.CLOB -> {
                Clob v = rs.getClob(columnIndex);
                if (returnNull(v, allowNulls)) {
                    return null;
                }
                value = (null != v) ? v.getCharacterStream() : new StringReader("");
            }
            default -> {
                value = rs.getObject(columnIndex);
                if (returnNull(value, allowNulls)) {
                    return null;
                }
            }
        }
        return TypeRegistry.scaleToType(value, columnType, returnType);
    }

    private static boolean returnNull(Object v, boolean allowNulls) {
        return ((null == v) && allowNulls);
    }

    /**
     * Return the argument type for columns in a resultSet.
     *
     * @param rs
     *
     * @return
     *
     * @throws SQLException
     */
    public static int[] rowTypes(ResultSet rs) throws SQLException {
        int[] paramTypes = new int[rs.getMetaData().getColumnCount()];
        for (int i = 0; i < paramTypes.length; i++) {
            int columnType = rs.getMetaData().getColumnType(i + 1);
            paramTypes[i] = (columnType);
        }
        return paramTypes;
    }

    public static Class<?>[] _rowTypes(ResultSet rs) throws SQLException {
        Class<?>[] paramTypes = new Class<?>[rs.getMetaData().getColumnCount()];
        for (int i = 0; i < paramTypes.length; i++) {
            int columnType = rs.getMetaData().getColumnType(i + 1);
            paramTypes[i] = TypeRegistry.SQL_TO_JAVA.get(columnType);
        }
        return paramTypes;
    }

    /**
     * Set the value of a statement parameter.
     *
     * @param ps
     * @param value
     * @param columnIndex
     *
     * @throws SQLException
     */
    public static void setValueForType(PreparedStatement ps, Object value, int columnIndex) throws SQLException {
        if (null == value) {
            ps.setObject(columnIndex, null);
            return;
        }

        if (value instanceof Byte b) {
            ps.setByte(columnIndex, b);
        } else if (value instanceof Byte[] boxed) {
            // convert Byte[] -> byte[]
            byte[] unboxed = new byte[boxed.length];
            for (int i = 0; i < boxed.length; i++) {
                unboxed[i] = (null != boxed[i]) ? boxed[i] : 0;
            }
            ps.setBytes(columnIndex, unboxed);
        } else if (value instanceof Integer i) {
            ps.setInt(columnIndex, i);
        } else if (value instanceof Short s) {
            ps.setShort(columnIndex, s);
        } else if (value instanceof Long l) {
            ps.setLong(columnIndex, l);
        } else if (value instanceof String s) {
            ps.setString(columnIndex, s);
        } else if (value instanceof Boolean b) {
            ps.setBoolean(columnIndex, b);
        } else if (value instanceof Float f) {
            ps.setFloat(columnIndex, f);
        } else if (value instanceof Double d) {
            ps.setDouble(columnIndex, d);
        } else if (value instanceof BigDecimal bd) {
            ps.setBigDecimal(columnIndex, bd);
        } else if (value instanceof BigInteger bi) {
            ps.setBigDecimal(columnIndex, new BigDecimal(bi));
        } else if (value instanceof LocalDate ld) {
            ps.setDate(columnIndex, java.sql.Date.valueOf(ld));
        } else if (value instanceof LocalTime lt) {
            ps.setTime(columnIndex, java.sql.Time.valueOf(lt));
        } else if (value instanceof Instant instant) {
            ps.setTimestamp(columnIndex, Timestamp.from(instant));
        } else if (value instanceof Date date) {
            ps.setDate(columnIndex, new java.sql.Date(date.getTime()));
        } else if (value instanceof ZonedDateTime zdt) {
            ps.setTimestamp(columnIndex, Timestamp.from(zdt.toInstant()));
        } else if (value instanceof URL || value instanceof URI) {
            ps.setString(columnIndex, value.toString());
        } else if (value instanceof byte[] bytes) {
            ps.setBytes(columnIndex, bytes);
        } else if (value instanceof InputStream is) {
            ps.setBinaryStream(columnIndex, is);
        } else if (value instanceof Reader reader) {
            ps.setCharacterStream(columnIndex, reader);
        } else {
            throw new SQLException("Object of type " + value.getClass().getName() + ", is not supported");
        }
    }

    /**
     * Return the null equivalent for a given column type.
     *
     * @param columnType
     *
     * @return
     */
    public static Object getNullForType(int columnType) {
        switch (columnType) {
            case Types.NUMERIC, Types.INTEGER, Types.SMALLINT -> {
                return 0;
            }
            case Types.DECIMAL, Types.DOUBLE, Types.FLOAT -> {
                return 0;
            }
            case Types.BOOLEAN -> {
                return false;
            }
            default -> {
                return null;
            }
        }
    }
}
