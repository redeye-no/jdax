package no.redeye.lib.jdax.types;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import no.redeye.lib.jdax.SQLTypeConverter;

/**
 * A transfer object for SQL results. ResultRows provides a mechanism to:
 * <li>retrieve query results, one row at a time</li>
 * <li>retrieve row results as Java records</li>
 */
public class ResultRows extends ResultSetType implements VO {

    public ResultRows(ResultSet resultSet, Statement statement, boolean allowNulls) throws SQLException {
        super(resultSet, statement, allowNulls);
    }

    /**
     * Get the results of the current row as a Java record of the given type.
     * This method requires that the row has values of the same type as the
     * record expects.
     *
     * @param <T>
     * @param returnType
     * @param <?>
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public <T> T get(Class<T> returnType) throws SQLException {
        try {
            Object[] argumentValues = resultSetValues();
            if (null == argumentTypes) {
                argumentTypes = SQLTypeConverter.rowTypes(resultSet);
            }
            if (argumentValues.length != argumentTypes.length) {
                throw new SQLException("DAOType expects " + argumentTypes.length + " parameters but ResultSet has " + argumentValues.length);
            }
            return returnType.getDeclaredConstructor(argumentTypes).newInstance(argumentValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Create and return an Object[] with the ResultSet values in the order
     * expected by the generic type constructor of the VO.
     *
     * @param rs
     *
     * @return
     *
     * @throws SQLException
     */
    private Object[] resultSetValues() throws SQLException {
        Object[] paramTypes = new Object[metaData.getColumnCount()];
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            int columnType = metaData.getColumnType(i);

            paramTypes[i - 1] = SQLTypeConverter.getValueForType(resultSet, i, columnType, allowNulls);
        }
        return paramTypes;
    }

    /**
     * Retrieve value of indexed field as a BigDecimal.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public BigDecimal bigDecimal(int index) throws SQLException {
        return getBigDecimal(index);
    }

    public BigDecimal bigDecimal(String fieldName) throws SQLException {
        return getBigDecimal(fieldName);
    }

    /**
     * Retrieve value of indexed field as a boolean.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public boolean booleanValue(int index) throws SQLException {
        return getBoolean(index);
    }

    public boolean booleanValue(String fieldName) throws SQLException {
        return getBoolean(fieldName);
    }

    /**
     * Retrieve value of indexed field as a byte.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public byte byteValue(int index) throws SQLException {
        return getByte(index);
    }

    public byte byteValue(String fieldName) throws SQLException {
        return getByte(fieldName);
    }

    /**
     * Retrieve value of indexed field as a double.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public double doubleValue(int index) throws SQLException {
        return getDouble(index);
    }

    public double doubleValue(String fieldName) throws SQLException {
        return getDouble(fieldName);
    }

    /**
     * Retrieve value of indexed field as a float.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public float floatValue(int index) throws SQLException {
        return getFloat(index);
    }

    public float floatValue(String fieldName) throws SQLException {
        return getFloat(fieldName);
    }

    /**
     * Retrieve value of indexed field as a .
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public int integerValue(int index) throws SQLException {
        return getInt(index);
    }

    public int integerValue(String fieldName) throws SQLException {
        return getInt(fieldName);
    }

    /**
     * Retrieve value of indexed field as a long value.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public long longValue(int index) throws SQLException {
        return getLong(index);
    }

    public long longValue(String fieldName) throws SQLException {
        return getLong(fieldName);
    }

    /**
     * Retrieve value of indexed field as a short value.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public short shortValue(int index) throws SQLException {
        return getShort(index);
    }

    public short shortValue(String fieldName) throws SQLException {
        return getShort(fieldName);
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalDateTime.
     *
     * @param index
     * @param zone
     * @return
     * @throws SQLException
     */
    public LocalDateTime dateTime(int index, ZoneId zone) throws SQLException {
        return LocalDateTime.ofInstant(timestamp(index), zone);
    }

    public LocalDateTime dateTime(String fieldName, ZoneId zone) throws SQLException {
        return dateTime(fieldNames.indexOf(fieldName), zone);
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalDate.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public LocalDate date(int index) throws SQLException {
        return getDate(index);
    }

    public LocalDate date(String fieldName) throws SQLException {
        return getDate(fieldName);
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalTime.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public LocalTime time(int index) throws SQLException {
        return getTime(index);
    }

    public LocalTime time(String fieldName) throws SQLException {
        return getTime(fieldName);
    }

    /**
     * Retrieve value of indexed field as a java.time.Instant.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public Instant timestamp(int index) throws SQLException {
        return getTimestamp(index);
    }

    public Instant timestamp(String fieldName) throws SQLException {
        return getTimestamp(fieldName);
    }

    /**
     * Retrieve value of indexed varchar
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public String varchar(int index) throws SQLException {
        return getString(index);
    }

    public String varchar(String fieldName) throws SQLException {
        return getString(fieldName);
    }

    /**
     * Retrieve value of indexed field as a byte array.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public byte[] binary(int index) throws SQLException {
        return getBytes(index);
    }

    public byte[] binary(String fieldName) throws SQLException {
        return getBytes(fieldName);
    }

    /**
     * Retrieve the InputStream associated with the current row field.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public InputStream stream(int index) throws SQLException {
        return getBinaryStream(index);
    }

    public InputStream stream(String fieldName) throws SQLException {
        return getBinaryStream(fieldName);
    }

    /**
     * Retrieve the Reader associated with the current row field.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public Reader reader(int index) throws SQLException {
        return getCharacterStream(index);
    }

    public Reader reader(String fieldName) throws SQLException {
        return getCharacterStream(fieldName);
    }

    private int columnIndex(String fieldName) {
        return fieldNames.indexOf(fieldName);
    }

}
