package no.redeye.lib.jdax.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import no.redeye.lib.jdax.jdbc.SQLTypeConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A transfer object for SQL results
 */
public class ResultRows implements VO {

    private final List<String> fieldNames = new ArrayList();
    private final List<List> rows = new ArrayList();
    private int rowIndex = -1;

    public ResultRows(ResultSet rs) throws SQLException {
        if (null != rs) {
            ResultSetMetaData metaData = rs.getMetaData();

            if (fieldNames.isEmpty()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (null != metaData.getColumnName(i)) {
                        fieldNames.add(metaData.getColumnLabel(i).toLowerCase());
                    }
                }
            }

            while (rs.next()) {
                List paramTypes = new ArrayList();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    int columnType = metaData.getColumnType(i);

                    Object obj = rs.getObject(i);

                    if (null == obj) {
                        paramTypes.add(SQLTypeConverter.getNullForType(columnType));
                    } else {
                        paramTypes.add(SQLTypeConverter.getValueForType(rs, i, columnType));
                    }
                }
                rows.add(paramTypes);
            }
        }
    }

    /**
     * Retrieve value of current field as an Object.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public Object object(int index) throws SQLException {
        if ((index < 0) || (index >= rows.get(rowIndex).size())) {
            throw new SQLException("Row index " + index + " is out of bounds, expected range is: 0 < index < " + rows.get(rowIndex).size());
        }
        return rows.get(rowIndex).get(index);
    }

    public Object object(String fieldName) throws SQLException {
        return object(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a BigDecimal.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public BigDecimal bigDecimal(int index) throws SQLException {
        return (BigDecimal) object(index);
    }

    public BigDecimal bigDecimal(String fieldName) throws SQLException {
        return bigDecimal(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a BigInteger.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public BigInteger bigInteger(int index) throws SQLException {
        return bigDecimal(index).toBigInteger();
    }

    public BigInteger bigInteger(String fieldName) throws SQLException {
        return bigDecimal(fieldNames.indexOf(fieldName)).toBigInteger();
    }

    /**
     * Retrieve value of current field as a boolean.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public boolean isTrue(int index) throws SQLException {
        return Boolean.parseBoolean("" + object(index));
    }

    public boolean booleanValue(String fieldName) throws SQLException {
        return isTrue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a byte.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public byte singleByte(int index) throws SQLException {
        return (byte) object(index);
    }

    public byte singleByte(String fieldName) throws SQLException {
        return singleByte(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a double.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public double doubleValue(int index) throws SQLException {
        return Double.parseDouble("" + object(index));
    }

    public double doubleValue(String fieldName) throws SQLException {
        return doubleValue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a float.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public float floatValue(int index) throws SQLException {
        return Float.parseFloat("" + object(index));
    }

    public float floatValue(String fieldName) throws SQLException {
        return floatValue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a .
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public int intValue(int index) throws SQLException {
        return (int) object(index);
    }

    public int intValue(String fieldName) throws SQLException {
        return intValue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a long value.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public long longValue(int index) throws SQLException {
        return Long.parseLong("" + object(index));
    }

    public long longValue(String fieldName) throws SQLException {
        return longValue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a short value.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public short shortValue(int index) throws SQLException {
        return Short.parseShort("" + object(index));
    }

    public short shortValue(String fieldName) throws SQLException {
        return shortValue(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a java.time.LocalDateTime.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public LocalDateTime dateTime(int index) throws SQLException {
        return ((LocalDateTime) object(index));
    }

    public LocalDateTime dateTime(String fieldName) throws SQLException {
        return dateTime(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a java.time.LocalDate.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public LocalDate date(int index) throws SQLException {
        return ((java.sql.Date) object(index)).toLocalDate();
    }

    public LocalDate date(String fieldName) throws SQLException {
        return date(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a java.time.LocalTime.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public LocalTime time(int index) throws SQLException {
        return ((Time) object(index)).toLocalTime();
    }

    public LocalTime time(String fieldName) throws SQLException {
        return time(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a java.time.Instant.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public Instant timestamp(int index) throws SQLException {
        return ((Timestamp) object(index)).toInstant();
    }

    public Instant timestamp(String fieldName) throws SQLException {
        return timestamp(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of current field as a String
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public String string(int index) throws SQLException {
        if (object(index) instanceof Reader reader) {
            try (BufferedReader br = new BufferedReader(reader)) {
                final StringBuilder sb = new StringBuilder();
                int b;
                while (-1 != (b = br.read())) {
                    sb.append((char) b);
                }
                return sb.toString();
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
        return String.valueOf(object(index));
    }

    public String string(String fieldName) throws SQLException {
        return string(fieldNames.indexOf(fieldName));
    }
    
    /**
     * Retrieve value of current field as a byte array.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public byte[] bytes(int index) throws SQLException {
        if (object(index) instanceof InputStream inputStream) {
            try (InputStream is = inputStream) {
                return is.readAllBytes();
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
        return (byte[]) object(index);
    }

    public byte[] bytes(String fieldName) throws SQLException {
        return bytes(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve the InputStream associated with the current row field.
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    public InputStream inputStream(int index) throws SQLException {
        if (object(index) instanceof InputStream inputStream) {
            return inputStream;
        }
        throw new SQLException("ClassCastException: type " + object(index) + " (column index " + index + ") cannot be cast ti an InputStream");
    }

    public InputStream inputStream(String fieldName) throws SQLException {
        return inputStream(fieldNames.indexOf(fieldName));
    }

    public int columnIndex(String fieldName) {
        return fieldNames.indexOf(fieldName);
    }
    
    public boolean next() {
        return (((++rowIndex) >= 0) && (rowIndex < rows.size()));
    }

    public int size() {
        return rows.size();
    }

}
