package no.redeye.lib.jdax.types;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
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
     * Retrieve value of indexed field as a java.time.LocalDateTime.
     *
     * @param index
     * @param zone
     * @return
     * @throws SQLException
     */
    public LocalDateTime dateTime(int index, ZoneId zone) throws SQLException {
        return LocalDateTime.ofInstant(getTimestamp(index), zone);
    }

    public LocalDateTime dateTime(String fieldName, ZoneId zone) throws SQLException {
        return dateTime(fieldNames.indexOf(fieldName), zone);
    }

    /**
     * Retrieve the value of a blob field as a byte array.
     *
     * @param index
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public byte[] blob(int index) throws SQLException, IOException {
        try (InputStream inStream = getBinaryStream(index); ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, n);
            }
            return outStream.toByteArray();
        }
    }

    public byte[] blob(String fieldName) throws SQLException, IOException {
        return blob(fieldNames.indexOf(fieldName));
    }

    /**
     * Retrieve value of a clob field as a String
     *
     * @param index
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public String clob(int index) throws SQLException, IOException {
        Reader reader = getCharacterStream(index);
        if (null == reader) {
            return "";
        }

        StringWriter sw = new StringWriter();

        try (BufferedReader br = new BufferedReader(reader)) {
            char[] buffer = new char[1024];
            int n;
            while ((n = br.read(buffer)) != -1) {
                sw.write(buffer, 0, n);
            }
        }
        return sw.toString();
    }

    public String clob(String fieldName) throws SQLException, IOException {
        return clob(fieldNames.indexOf(fieldName));
    }

    private int columnIndex(String fieldName) {
        return fieldNames.indexOf(fieldName);
    }

}