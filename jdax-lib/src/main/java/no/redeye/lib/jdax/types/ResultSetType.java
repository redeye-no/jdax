package no.redeye.lib.jdax.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import no.redeye.lib.jdax.SQLTypeConverter;

/**
 * A transfer object for SQL results. ResultRows provides a mechanism to:
 * <li>retrieve query results, one row at a time</li>
 * <li>retrieve row results as Java records</li>
 */
public abstract class ResultSetType implements AutoCloseable {

    protected final List<String> fieldNames = new ArrayList();

    protected final Statement statement;
    protected final ResultSet resultSet;
    protected ResultSetMetaData metaData;
    protected Class<?>[] argumentTypes = null;
    private List rowTypes = new ArrayList();
    protected final boolean allowNulls;

    public ResultSetType(ResultSet resultSet, Statement statement, boolean allowNulls) throws SQLException {
        this.allowNulls = allowNulls;
        this.statement = statement;
        this.resultSet = resultSet;
        if (null != resultSet) {
            metaData = resultSet.getMetaData();

            if (fieldNames.isEmpty()) {
                fieldNames.add("noop");
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (null != metaData.getColumnName(i)) {
                        fieldNames.add(metaData.getColumnLabel(i).toLowerCase());
                    }
                }
            }
        }
    }

    public boolean next() throws SQLException {
        rowTypes.clear();
        return ((null != resultSet) && resultSet.next());
    }

    @Override
    public void close() throws IOException, SQLException {
        metaData = null;
        argumentTypes = null;
        rowTypes = null;

        try (resultSet) {
            try (statement) {
            }
        }
    }

    /**
     * Retrieve value of indexed field as an Object. Index starts at 1, not 0.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public Object getObject(int index) throws SQLException {
        return getObject(index, Integer.MAX_VALUE);
    }

    /**
     * Retrieve value of named field as an Object.
     *
     * @param fieldName
     * @return
     * @throws SQLException
     */
    public Object getObject(String fieldName) throws SQLException {
        return object(fieldName, Integer.MAX_VALUE);
    }
    public Object getObject(String fieldName, int returnType) throws SQLException {
        return object(fieldName, returnType);
    }
    private Object object(String fieldName, int returnType) throws SQLException {
        return getObject(fieldNames.indexOf(fieldName), returnType);
    }
	
	
	/**
	 * Automatic type scaling for numeric values.
	 * @param fieldName
	 * @param returnType
	 * @return
	 * @throws SQLException 
	 */
    private Object getObject(int index, int returnType) throws SQLException {
        if ((index < 1) || (index > metaData.getColumnCount())) {
            throw new SQLException("Row index " + index + " is out of bounds, expected range is: 1 <= index <= " + metaData.getColumnCount());
        }
        int columnType = metaData.getColumnType(index);
        return (SQLTypeConverter.getValueForType(resultSet, index, columnType, allowNulls, returnType));
    }
    
    /**
     * Retrieve value of indexed field as a BigDecimal.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public BigDecimal getBigDecimal(int index) throws SQLException {
        return (BigDecimal) getObject(index,Types.DECIMAL);
    }

    public BigDecimal getBigDecimal(String fieldName) throws SQLException {
        return getBigDecimal(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a BigInteger.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public BigInteger getBigInteger(int index) throws SQLException {
        BigDecimal o = getBigDecimal(index);
        return (null != o) ? o.toBigInteger() : allowNulls ? null : BigInteger.ZERO;
    }

    public BigInteger getBigInteger(String fieldName) throws SQLException {
        return getBigInteger(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a boolean.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public boolean getBoolean(int index) throws SQLException {
        return (boolean) getObject(index);
    }

    public boolean getBoolean(String fieldName) throws SQLException {
        return getBoolean(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a byte.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public byte getByte(int index) throws SQLException {
        return (byte) getObject(index);
    }

    public byte getByte(String fieldName) throws SQLException {
        return getByte(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a double.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public double getDouble(int index) throws SQLException {
        return (double) getObject(index, Types.DOUBLE);
    }

    public double getDouble(String fieldName) throws SQLException {
        return getDouble(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a float.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public float getFloat(int index) throws SQLException {
        return (float) getObject(index, Types.REAL);
    }

    public float getFloat(String fieldName) throws SQLException {
        return getFloat(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a .
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public int getInt(int index) throws SQLException {
        return (int) getObject(index, Types.INTEGER);
    }

    public int getInt(String fieldName) throws SQLException {
        return getInt(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a long value.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public long getLong(int index) throws SQLException {
        return (long) getObject(index, Types.BIGINT);
    }

    public long getLong(String fieldName) throws SQLException {
        return getLong(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a short value.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public short getShort(int index) throws SQLException {
        return (short) getObject(index, Types.SMALLINT);
    }

    public short getShort(String fieldName) throws SQLException {
        return getShort(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalDate.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public LocalDate getDate(int index) throws SQLException {
        return (LocalDate) getObject(index,Types.DATE);
    }

    public LocalDate getDate(String fieldName) throws SQLException {
        return getDate(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalTime.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public LocalTime getTime(int index) throws SQLException {
        return (LocalTime) getObject(index,Types.TIME);
    }

    public LocalTime getTime(String fieldName) throws SQLException {
        return getTime(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a java.time.Instant.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public Instant getTimestamp(int index) throws SQLException {
        return (Instant) getObject(index,Types.TIMESTAMP);
    }

    public Instant getTimestamp(String fieldName) throws SQLException {
        return getTimestamp(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed varchar
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public String getString(int index) throws SQLException {
        return (String) getObject(index,Types.VARCHAR);
    }

    public String getString(String fieldName) throws SQLException {
        return getString(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of indexed field as a byte array.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public byte[] getBytes(int index) throws SQLException {
        return (byte[]) getObject(index);
    }

    public byte[] getBytes(String fieldName) throws SQLException {
        return getBytes(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve the InputStream associated with the current row field.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public InputStream getBinaryStream(int index) throws SQLException {
        return (InputStream) getObject(index);
    }

    public InputStream getBinaryStream(String fieldName) throws SQLException {
        return getBinaryStream(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve the Reader associated with the current row field.
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public Reader getCharacterStream(int index) throws SQLException {
        return (Reader) getObject(index);
    }

    public Reader getCharacterStream(String fieldName) throws SQLException {
        return getCharacterStream(fieldNames.indexOf(fieldName));
    }
}
