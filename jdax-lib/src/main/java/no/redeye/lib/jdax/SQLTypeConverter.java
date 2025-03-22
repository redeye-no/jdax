package no.redeye.lib.jdax;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
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
public class SQLTypeConverter {

	/**
	 * Return a resultSet value of the stored object type.
	 *
	 * @param rs
	 * @param columnIndex
	 * @param columnType
	 * @param allowNulls
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	
	public static Object getValueForType(ResultSet rs, int columnIndex, int columnType, boolean allowNulls) throws SQLException {
		return getValueForType(rs, columnIndex, columnType, allowNulls, Integer.MAX_VALUE);
	}

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
	
	public static Object getValueForType(ResultSet rs, int columnIndex, int columnType, boolean allowNulls, int returnType) throws SQLException {
		Object value;
		switch (columnType) {
			case Types.VARCHAR, Types.CHAR, Types.LONGNVARCHAR -> {
				String s = rs.getString(columnIndex);
				value = ((null != s) && !rs.wasNull()) ? s : allowNulls ? null : "";
			}
			case Types.DECIMAL, Types.NUMERIC -> {
				// NUMBER(p, s)
				BigDecimal bd = rs.getBigDecimal(columnIndex);
				value = ((null != bd) && !rs.wasNull()) ? bd : allowNulls ? null : BigDecimal.ZERO;
			}
			case Types.BIT, Types.BOOLEAN -> {
				// NUMBER(3)
				value = rs.getBoolean(columnIndex);
			}
			case Types.SMALLINT -> {
				// NUMBER(5)
				value = rs.getShort(columnIndex);
			}
			case Types.INTEGER -> {
				// NUMBER(10)
				value = rs.getInt(columnIndex);
			}
			case Types.TINYINT -> {
				value = rs.getByte(columnIndex);
			}
			case Types.BIGINT -> {
				// NUMBER(19)
				value = rs.getLong(columnIndex);
			}
			case Types.DOUBLE, Types.FLOAT -> {
				// FLOAT(49)
				value = rs.getDouble(columnIndex);
			}
			case Types.REAL -> {
				// FLOAT(23)
				value = rs.getFloat(columnIndex);
			}
			case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
				byte[] bytes = rs.getBytes(columnIndex);
				value = ((null != bytes) && !rs.wasNull()) ? bytes : allowNulls ? null : new byte[0];
			}
			case Types.NULL -> {
				return null;
			}
			case Types.DATE -> {
				java.sql.Date date = rs.getDate(columnIndex);
				value = ((null != date) && !rs.wasNull()) ? date.toLocalDate() : allowNulls ? null : LocalDate.EPOCH;
			}
			case Types.TIME, Types.TIME_WITH_TIMEZONE -> {
				Time time = rs.getTime(columnIndex);
				value = ((null != time) && !rs.wasNull()) ? time.toLocalTime() : allowNulls ? null : LocalTime.MIDNIGHT;
			}
			case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
				Timestamp timestamp = rs.getTimestamp(columnIndex);
				value = ((null != timestamp) && !rs.wasNull()) ? timestamp.toInstant() : allowNulls ? null : Instant.EPOCH;
			}
			case Types.BLOB -> {
				Blob blob = rs.getBlob(columnIndex);
				value = ((null != blob) && !rs.wasNull()) ? blob.getBinaryStream() : allowNulls ? null : new EmptyStream();
			}
			case Types.CLOB -> {
				Clob clob = rs.getClob(columnIndex);
				value = ((null != clob) && !rs.wasNull()) ? clob.getCharacterStream() : allowNulls ? null : new StringReader("");
			}
			default -> {
				value = rs.getObject(columnIndex);
			}
		}
		return (returnType < Integer.MAX_VALUE) ? scaleToType(value, returnType) : value;
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
	public static Class<?>[] rowTypes(ResultSet rs) throws SQLException {
		Class<?>[] paramTypes = new Class<?>[rs.getMetaData().getColumnCount()];
		for (int i = 0; i < paramTypes.length; i++) {
			int columnType = rs.getMetaData().getColumnType(i + 1);
			paramTypes[i] = columnTypeClass(columnType);
		}
		return paramTypes;
	}

	private static Class columnTypeClass(int columnType) throws SQLException {
		switch (columnType) {
			case Types.VARCHAR, Types.CHAR, Types.LONGNVARCHAR -> {
				return String.class;
			}
			case Types.DECIMAL, Types.NUMERIC -> {
				// NUMBER(p, s)
				return BigDecimal.class;
			}
			case Types.BIT -> {
				// NUMBER(3)
				return boolean.class;
			}
			case Types.SMALLINT -> {
				// NUMBER(5)
				return short.class;
			}
			case Types.INTEGER -> {
				// NUMBER(10)
				return int.class;
			}
			case Types.TINYINT -> {
				return byte.class;
			}
			case Types.BIGINT -> {
				// NUMBER(19)
				return long.class;
			}
			case Types.DOUBLE, Types.FLOAT -> {
				// FLOAT(49)
				return double.class;
			}
			case Types.REAL -> {
				// FLOAT(23)
				return float.class;
			}
			case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> {
				return byte[].class;
			}
			case Types.NULL -> {
				return null;
			}
			case Types.DATE -> {
				return LocalDate.class;
			}
			case Types.TIME -> {
				return LocalTime.class;
			}
			case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> {
				return Instant.class;
			}
			case Types.BLOB -> {
				return InputStream.class;
			}
			case Types.CLOB -> {
				return Reader.class;
			}
			default -> {
				return Object.class;
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
			case "java.lang.Integer", "int" -> {
				ps.setInt(columnIndex, (int) value);
			}
			case "java.lang.Short", "short" -> {
				ps.setShort(columnIndex, (short) value);
			}
			case "java.lang.Long", "long" -> {
				ps.setLong(columnIndex, (Long) value);
			}
			case "java.lang.String" -> {
				ps.setString(columnIndex, (String) value);
			}
			case "java.lang.Boolean", "boolean" -> {
				ps.setBoolean(columnIndex, (Boolean) value);
			}
			case "java.lang.Float", "float" -> {
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
				} else if (value.getClass().getName().toLowerCase().contains("reader")) {
					ps.setCharacterStream(columnIndex, (Reader) value);
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

	/**
	 * JDAX Type scaling
	 * @param value
	 * @param returnType
	 * @return
	 */
	private static Object scaleToType(Object value, int returnType) {
		if (null == value) {
			return value;
		}
		
		return switch (returnType) {
			case Types.INTEGER -> {
				if (value instanceof BigDecimal bd) {
					yield bd.intValue();
				}
				if (value instanceof Long l) {
					yield l.intValue();
				}
				if (value instanceof Double d) {
					yield d.intValue();
				}
				if (value instanceof Short s) {
					yield (int) s;
				}
				if (value instanceof Byte b) {
					yield (int) b;
				}
				yield value;
			}
			case Types.BIGINT -> {
				if (value instanceof BigDecimal bd) {
					yield bd.longValue();
				}
				if (value instanceof Integer i) {
					yield (long) i;
				}
				if (value instanceof Double d) {
					yield d.longValue();
				}
				if (value instanceof Short s) {
					yield (long) s;
				}
				if (value instanceof Byte b) {
					yield (long) b;
				}
				yield value;
			}
			case Types.DECIMAL, Types.NUMERIC -> {
				if (value instanceof Long l) {
					yield BigDecimal.valueOf(l);
				}
				if (value instanceof Integer i) {
					yield BigDecimal.valueOf(i);
				}
				if (value instanceof Double d) {
					yield BigDecimal.valueOf(d);
				}
				if (value instanceof Short s) {
					yield BigDecimal.valueOf(s);
				}
				if (value instanceof Byte b) {
					yield BigDecimal.valueOf(b);
				}
				yield value;
			}
			case Types.FLOAT, Types.REAL, Types.DOUBLE -> {
				if (value instanceof BigDecimal bd) {
					yield bd.doubleValue();
				}
				if (value instanceof Long l) {
					yield l.doubleValue();
				}
				if (value instanceof Integer i) {
					yield i.doubleValue();
				}
				if (value instanceof Short s) {
					yield s.doubleValue();
				}
				if (value instanceof Byte b) {
					yield b.doubleValue();
				}
				yield value;
			}
			default ->{
				yield value;
			}
		};
	}
}
