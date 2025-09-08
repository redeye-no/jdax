package no.redeye.lib.jdax;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import no.redeye.lib.jdax.types.ResultRows;

/**
 */
public class JDAXScaleTests {

    private static ResultSet mockResultSet;

    @BeforeAll
    static void setUpAll() throws SQLException {
        mockResultSet = Mockito.mock(ResultSet.class);
    }

    private ResultRows resultRowsFor(int type, boolean allowNulls) throws SQLException {
        Statement mockStatement = Mockito.mock(Statement.class);

        Mockito.when(mockResultSet.wasNull()).thenReturn(allowNulls);

        ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(metaData.getColumnCount()).thenReturn(1);
        Mockito.when(metaData.getColumnType(1)).thenReturn(type);
        Mockito.when(metaData.getColumnName(1)).thenReturn("field");
        Mockito.when(metaData.getColumnLabel(1)).thenReturn("field");

        Mockito.when(mockResultSet.getMetaData()).thenReturn(metaData);

        ResultRows resultRows = Mockito.spy(new ResultRows(mockResultSet, mockStatement, allowNulls));

        return resultRows;
    }

    private ResultRows assertTypeScaling(int columnType, int resultType, Object expected, boolean allowNulls) throws SQLException {
        ResultRows resultRowsFor = resultRowsFor(columnType, allowNulls);
        Object returned = resultRowsFor.getObject("field", resultType);

        Assertions.assertEquals(expected, returned);
        return resultRowsFor;
    }

    @Test
    private void testUnscaledType() throws SQLException {

        Object result = TypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, true, Integer.class);
        Assertions.assertNull(result);

        result = TypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, false, Integer.class);
        Assertions.assertInstanceOf(InputStream.class, result);

        Blob persisted = null;

        Mockito.when(mockResultSet.getBlob(1)).thenReturn(persisted);
        assertTypeScaling(Types.BLOB, Types.INTEGER, null, true);
    }

    @Test
    private void testUnsupportedType() throws SQLException {
        int unsupported = Integer.MAX_VALUE - 1;
        Blob persisted = null;

        Mockito.when(mockResultSet.getBlob(1)).thenReturn(persisted);
        assertTypeScaling(Types.BLOB, unsupported, null, true);
    }

    // --
    @FunctionalInterface
    private interface ResultRowsGetXFunction<T, R> {

        R apply(T t) throws SQLException, IOException, ClassCastException;
    }

    private static Stream<Arguments> resultRowsGetters() {
        return Stream.of(
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBigInteger("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBinaryStream("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.blob("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBoolean("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getByte("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBytes("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getCharacterStream("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getDate("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getDouble("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getFloat("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getInt("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getLong("field")),
                //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getObject("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getShort("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getString("field"))
        //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getTime("field")),
        //                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getTimestamp("field"))
        );
    }

    @ParameterizedTest
    @MethodSource("resultRowsGetters")
    void testNullHandling(ResultRowsGetXFunction<ResultRows, Object> resultRowsGet) throws SQLException, IOException {
        BigDecimal persisted = null;
        Mockito.when(mockResultSet.getBigDecimal(1)).thenReturn(null);

        ResultRows nullRow = assertTypeScaling(Types.DECIMAL, Types.INTEGER, null, true);
        ResultRows defaultRow = assertTypeScaling(Types.DECIMAL, Types.DECIMAL, BigDecimal.ZERO, false);

        try {
            Assertions.assertNotNull(resultRowsGet.apply(defaultRow), "Expected non-null for defaultRow");
        } catch (ClassCastException cce) {
        }
    }
}
