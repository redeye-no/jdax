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
import java.util.stream.IntStream;
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

    private ResultRows assertTypeScaling(
            int columnType,
            int resultType,
            Object expected,
            boolean allowNulls
    ) throws SQLException {

        ResultRows rows = resultRowsFor(columnType, allowNulls);
        Object returned = rows.getObject("field", resultType);

        if (expected instanceof Number && returned instanceof Number) {
            Assertions.assertEquals(
                    0,
                    new BigDecimal(expected.toString())
                            .compareTo(new BigDecimal(returned.toString()))
            );
        } else {
            System.out.println("columnType=" + columnType + " resultType=" + resultType + " expected=" + expected + " expected=" + expected);
            Assertions.assertEquals(expected, expected);
        }

        return rows;
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
    private static Object defaultValueForType(int sqlType) {
        return switch (sqlType) {

            // Exact numeric
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER ->
                0;

            case Types.BIGINT ->
                0L;

            case Types.DECIMAL, Types.NUMERIC ->
                BigDecimal.ZERO;

            // Approx numeric
            case Types.FLOAT, Types.REAL, Types.DOUBLE ->
                0.0d;

            // Character
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR ->
                "";

            // Boolean
            case Types.BOOLEAN, Types.BIT ->
                false;

            // Date / time
            case Types.DATE ->
                java.sql.Date.valueOf("1970-01-01");
            case Types.TIME ->
                java.sql.Time.valueOf("00:00:00");
            case Types.TIMESTAMP ->
                java.sql.Timestamp.valueOf("1970-01-01 00:00:00");

            // Binary
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY ->
                new byte[0];

            default ->
                null; // explicit: unsupported / not tested
        };
    }

    private static final int[] EXACT_NUMERIC = {
        Types.TINYINT,
        Types.SMALLINT,
        Types.INTEGER,
        Types.BIGINT,
        Types.DECIMAL,
        Types.NUMERIC
    };

    private static final int[] APPROX_NUMERIC = {
        Types.FLOAT,
        Types.REAL,
        Types.DOUBLE
    };

    private static Stream<Arguments> typeScalingArguments() {
        return Stream.of(
                exactToExact(),
                exactToApprox(),
                approxToExact(),
                approxToApprox()
        ).flatMap(s -> s);
    }

    private static Stream<Arguments> exactToExact() {
        return combinations(EXACT_NUMERIC, EXACT_NUMERIC, false);
    }

    private static Stream<Arguments> exactToApprox() {
        return combinations(EXACT_NUMERIC, APPROX_NUMERIC, true);
    }

    private static Stream<Arguments> approxToExact() {
        return combinations(APPROX_NUMERIC, EXACT_NUMERIC, true);
    }

    private static Stream<Arguments> approxToApprox() {
        return combinations(APPROX_NUMERIC, APPROX_NUMERIC, false);
    }

    private static Stream<Arguments> combinations(
            int[] sources,
            int[] targets,
            boolean expectScaling
    ) {
        return IntStream.of(sources)
                .boxed()
                .flatMap(src
                        -> IntStream.of(targets)
                        .mapToObj(tgt
                                -> Arguments.of(
                                src,
                                tgt,
                                defaultValueForType(src), // 👈 source-based value
                                expectScaling
                        )
                        )
                );
    }

    @ParameterizedTest(name = "src={0}, tgt={1}, value={2}, scaled={3}")
    @MethodSource("typeScalingArguments")
    void assert_type_scaling(
            int sourceType,
            int targetType,
            Object value,
            boolean expected
    ) throws SQLException {
        assertTypeScaling(sourceType, targetType, value, expected);
    }

    // --
    @FunctionalInterface
    private interface ResultRowsGetXFunction<T, R> {

        R apply(T t) throws SQLException, IOException, ClassCastException;
    }

    private static Stream<Arguments> resultRowsGetters() {
        return Stream.of(
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBigInteger("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBinaryStream("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.blob("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBoolean("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getByte("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getBytes("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getCharacterStream("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getDate("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getDouble("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getFloat("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getInt("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getLong("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getObject("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getShort("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getString("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getTime("field")),
                Arguments.of((ResultRowsGetXFunction<ResultRows, Object>) row -> row.getTimestamp("field"))
        );
    }

    @ParameterizedTest
    @MethodSource("resultRowsGetters")
    void testNullHandling(ResultRowsGetXFunction<ResultRows, Object> resultRowsGet) throws SQLException, IOException {
        BigDecimal persisted = null;
//        Mockito.when(mockResultSet.getBigDecimal(Mockito.any())).thenReturn(null);

        ResultRows nullRow = assertTypeScaling(Types.DECIMAL, Types.INTEGER, null, true);
        ResultRows defaultRow = assertTypeScaling(Types.DECIMAL, Types.DECIMAL, BigDecimal.ZERO, false);

        try {
            Assertions.assertNotNull(resultRowsGet.apply(defaultRow), "Expected non-null for defaultRow");
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }
    }
}
