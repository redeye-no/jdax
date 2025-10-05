package no.redeye.lib.jdax.types;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import no.redeye.lib.jdax.TypeConverter;
import no.redeye.lib.jdax.TypeRegistry;

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
            if (null == resultSetTypes) {
                resultSetTypes = TypeConverter.rowTypes(resultSet);
            }

            Constructor<T> constructor = findConstructors(returnType, resultSetTypes);
            Class<?>[] constructorParamTypes = constructor.getParameterTypes();
            Object[] resultSetValues = resultSetValues(constructorParamTypes);

            if (resultSetValues.length != resultSetTypes.length) {
                throw new SQLException("DAOType expects " + resultSetTypes.length + " parameters but ResultSet has " + resultSetValues.length);
            }

            return constructor.newInstance(resultSetValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Find constructors whose parameter types are compatible
     * with the requested types (using TypeRegistry rules).
     *
     * @param <T>
     * @param clazz
     * @param resultSetTypes
     *
     * @return
     *
     * @throws java.lang.NoSuchMethodException
     */
    public static <T> Constructor<T> findConstructors(Class<T> clazz, int[] resultSetTypes) throws NoSuchMethodException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Class<?>[] actualParamTypes = constructor.getParameterTypes();
            if (convertible(resultSetTypes, actualParamTypes)) {
                @SuppressWarnings("unchecked")
                Constructor<T> c = (Constructor<T>) constructor;
                return c;
            }
        }
        throw new NoSuchMethodException("Unsupported conversion type, " + clazz);
    }

    private static boolean convertible(int[] resultSetTypes, Class<?>[] actual) {
        if (actual.length != resultSetTypes.length) {
            return false;
        }

        for (int i = 0; i < actual.length; i++) {
            if (!TypeRegistry.isCompatible(resultSetTypes[i], actual[i])) {
                return false;
            }
        }
        return true;
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
    private Object[] resultSetValues(Class<?>[] requestedParameterTypes) throws SQLException {
        Object[] values = new Object[requestedParameterTypes.length];
        for (int i = 0; i < requestedParameterTypes.length; i++) {
            int columnIndex = (i + 1);
            int columnType = metaData.getColumnType(columnIndex);
            Class<?> targetType = requestedParameterTypes[i];
            values[i] = TypeConverter.getValueForType(resultSet, columnIndex, columnType, allowNulls, targetType);
        }

        dumpit(requestedParameterTypes, values);
        return values;
    }

    private void dumpit(Object[] expected, Object[] values) {
        System.out.println("\n######################################");

        for (int i = 0; i < expected.length; i++) {
            System.out.printf("Param[%d]: expected=%s, \t\tgot=%s/%s%n",
                    i,
                    (expected[i] == null ? "null" : expected[i]),
                    (values[i] == null ? "null" : values[i].getClass().getName()),
                    (values[i] == null ? "null" : values[i]));
        }
    }

    /**
     * Retrieve value of indexed field as a java.time.LocalDateTime.
     *
     * @param index
     * @param zone
     *
     * @return
     *
     * @throws SQLException
     */
    public LocalDateTime dateTime(int index, ZoneId zone) throws SQLException {
        return LocalDateTime.ofInstant(getTimestamp(index), zone);
    }

    public LocalDateTime dateTime(String fieldName, ZoneId zone) throws SQLException {
        return dateTime(fieldNames.indexOf(fieldName.toLowerCase()), zone);
    }

    /**
     * Retrieve the value of a blob field as a byte array.
     *
     * @param index
     *
     * @return
     *
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
        return blob(fieldNames.indexOf(fieldName.toLowerCase()));
    }

    /**
     * Retrieve value of a clob field as a String
     *
     * @param index
     *
     * @return
     *
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
        return clob(fieldNames.indexOf(fieldName.toLowerCase()));
    }
}
