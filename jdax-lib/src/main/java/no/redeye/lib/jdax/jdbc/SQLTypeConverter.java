package no.redeye.lib.jdax.jdbc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 *
 */
public class SQLTypeConverter {

    /**
     * Return a resultSet value of the specified type.
     *
     * @param rs
     * @param columnIndex
     * @param columnType
     *
     * @return
     *
     * @throws SQLException
     */
    public static Object getValueForType(ResultSet rs, int columnIndex, int columnType) throws SQLException {
        switch (columnType) {
            case Types.VARCHAR, Types.CHAR, Types.LONGNVARCHAR -> {
                return rs.getString(columnIndex);
            }
            case Types.DECIMAL, Types.NUMERIC -> {
                // NUMBER(p, s)
                return rs.getBigDecimal(columnIndex);
            }
            case Types.BIT -> {
                // NUMBER(3)
                return rs.getBoolean(columnIndex);
            }
            case Types.SMALLINT -> {
                // NUMBER(5)
                return rs.getShort(columnIndex);
            }
            case Types.INTEGER -> {
                // NUMBER(10)
                return rs.getInt(columnIndex);
            }
            case Types.TINYINT -> {
                return rs.getByte(columnIndex);
            }
            case Types.BIGINT -> {
                // NUMBER(19)
                return rs.getLong(columnIndex);
            }
            case Types.DOUBLE, Types.FLOAT -> {
                // FLOAT(49)
                return rs.getDouble(columnIndex);
            }
            case Types.REAL -> {
                // FLOAT(23)
                return rs.getFloat(columnIndex);
            }
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
                return rs.getBytes(columnIndex);
            }
            case Types.NULL -> {
                return null;
            }
            case Types.DATE -> {
                return rs.getDate(columnIndex);
            }
            case Types.TIME -> {
                return rs.getTime(columnIndex);
            }
            case Types.TIMESTAMP -> {
                return rs.getTimestamp(columnIndex);
            }
            case Types.BLOB -> {
                return rs.getBlob(columnIndex).getBinaryStream();
            }
            case Types.CLOB -> {
                return rs.getClob(columnIndex).getCharacterStream();
            }
            default -> {
                return rs.getObject(columnIndex);
            }
        }
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

        switch (value.getClass().getName()) {
            case "java.lang.Integer" -> {
                ps.setInt(columnIndex, (int) value);
            }
            case "java.lang.Long" -> {
                ps.setLong(columnIndex, (Long) value);
            }
            case "java.lang.String" -> {
                ps.setString(columnIndex, (String) value);
            }
            case "java.lang.Boolean" -> {
                ps.setBoolean(columnIndex, (Boolean) value);
            }
            case "java.lang.Float" -> {
                ps.setFloat(columnIndex, (Float) value);
            }
            case "java.lang.Double" -> {
                ps.setDouble(columnIndex, (Double) value);
            }
            case "java.math.BigDecimal" -> {
                ps.setBigDecimal(columnIndex, (BigDecimal) value);
            }
            case "java.time.LocalDate" -> {
                ps.setDate(columnIndex, java.sql.Date.valueOf((LocalDate) value));
            }
            case "java.time.LocalTime" -> {
                ps.setTime(columnIndex, java.sql.Time.valueOf((LocalTime) value));
            }
            case "java.time.Instant" -> {
                ps.setTimestamp(columnIndex, new Timestamp(((Instant) value).toEpochMilli()));
            }
            case "java.util.Date" -> {
                ps.setDate(columnIndex, new java.sql.Date(((Date) value).getTime()));
            }
            case "java.time.ZonedDateTime" -> {
                ps.setTimestamp(columnIndex, new Timestamp(((ZonedDateTime) value).toInstant().toEpochMilli()));
            }
            case "java.new.URL", "java.new.URI" -> {
                ps.setString(columnIndex, value.toString());
            }
            case "[B" -> {
                ps.setBytes(columnIndex, (byte[]) value);
            }
            default -> {
                if (value.getClass().getName().toLowerCase().contains("inputstream")) {
                    ps.setBinaryStream(columnIndex, (InputStream) value);
                } else {
                    throw new SQLException("Object of type " + value.getClass().getName() + ", is not supported");
                }
            }
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
