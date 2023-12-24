package no.redeye.lib.jdax.types;

import no.redeye.lib.jdax.jdbc.SQLTypeConverter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    public Object getObject(int index) throws SQLException {
        if ((index < 0) || (index >= rows.get(rowIndex).size())) {
            throw new SQLException("Row index " + index + " is out of bounds, expected range is: 0 < index < " + rows.get(rowIndex).size());
        }
        return rows.get(rowIndex).get(index);
    }

    public Object getObject(String fieldName) throws SQLException {
        return getObject(fieldNames.indexOf(fieldName));
    }

    public BigDecimal getBigDecimal(int index) throws SQLException {
        return (BigDecimal) getObject(index);
    }

    public BigDecimal getBigDecimal(String fieldName) throws SQLException {
        return getBigDecimal(fieldNames.indexOf(fieldName));
    }

    public Boolean getBoolean(int index) throws SQLException {
        return (Boolean) getObject(index);
    }

    public Boolean getBoolean(String fieldName) throws SQLException {
        return getBoolean(fieldNames.indexOf(fieldName));
    }

    public byte getByte(int index) throws SQLException {
        return (byte) getObject(index);
    }

    public byte getByte(String fieldName) throws SQLException {
        return getByte(fieldNames.indexOf(fieldName));
    }

    public byte[] getBytes(int index) throws SQLException {
        return (byte[]) getObject(index);
    }

    public byte[] getBytes(String fieldName) throws SQLException {
        return getBytes(fieldNames.indexOf(fieldName));
    }

    public Double getDouble(int index) throws SQLException {
        return (Double) getObject(index);
    }

    public Double getDouble(String fieldName) throws SQLException {
        return getDouble(fieldNames.indexOf(fieldName));
    }

    public Float getFloat(int index) throws SQLException {
        return (Float) getObject(index);
    }

    public Float getFloat(String fieldName) throws SQLException {
        return getFloat(fieldNames.indexOf(fieldName));
    }

    public int getInt(int index) throws SQLException {
        return (int) getObject(index);
    }

    public int getInt(String fieldName) throws SQLException {
        return getInt(fieldNames.indexOf(fieldName));
    }

    public Long getLong(int index) throws SQLException {
        return (Long) getObject(index);
    }

    public Long getLong(String fieldName) throws SQLException {
        return getLong(fieldNames.indexOf(fieldName));
    }

    public Short getShort(int index) throws SQLException {
        return (Short) getObject(index);
    }

    public Short getShort(String fieldName) throws SQLException {
        return getShort(fieldNames.indexOf(fieldName));
    }

    public String getString(int index) throws SQLException {
        return (String) getObject(index);
    }

    public String getString(String fieldName) throws SQLException {
        return getString(fieldNames.indexOf(fieldName));
    }

    public Date getTime(int index) throws SQLException {
        return (Date) getObject(index);
    }

    public Date getTime(String fieldName) throws SQLException {
        return getTime(fieldNames.indexOf(fieldName));
    }

    public ZonedDateTime getTimestamp(int index) throws SQLException {
        return ZonedDateTime.ofInstant(((Timestamp) getObject(index)).toInstant(), ZoneId.of("UTC"));
    }

    public ZonedDateTime getTimestamp(String fieldName) throws SQLException {
        return getTimestamp(fieldNames.indexOf(fieldName));
    }

    public boolean next() {
        return (((++rowIndex) >= 0) && (rowIndex < rows.size()));
    }

    public int size() {
        return rows.size();
    }
}
