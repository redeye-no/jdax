package no.redeye.lib.jdax;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDAXScaleTests {

	@Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        mockResultSet = Mockito.mock(ResultSet.class);
    }

    @Test
    void testBigDecimalToInteger() throws SQLException {
        Mockito.lenient().when(mockResultSet.getBigDecimal(1)).thenReturn(new BigDecimal("123.7"));
        Mockito.lenient().when(mockResultSet.wasNull()).thenReturn(false);

        Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.DECIMAL, false, Types.INTEGER);
        Assertions.assertEquals(123, result); // Truncated to nearest int
    }

    @Test
    void testBigDecimalToLong() throws SQLException {
        Mockito.lenient().when(mockResultSet.getBigDecimal(1)).thenReturn(new BigDecimal("98765.9"));
        Mockito.lenient().when(mockResultSet.wasNull()).thenReturn(false);

        Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.DECIMAL, false, Types.BIGINT);
        Assertions.assertEquals(98765L, result);
    }

    @Test
    void testLongToDouble() throws SQLException {
        Mockito.lenient().when(mockResultSet.getLong(1)).thenReturn(123456789L);
        Mockito.lenient().when(mockResultSet.wasNull()).thenReturn(false);

        Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.BIGINT, false, Types.FLOAT);
        Assertions.assertEquals(123456789.0, result);
    }

    @Test
    void testIntegerToBigDecimal() throws SQLException {
        Mockito.lenient().when(mockResultSet.getInt(1)).thenReturn(42);
        Mockito.lenient().when(mockResultSet.wasNull()).thenReturn(false);

        Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.INTEGER, false, Types.DECIMAL);
        Assertions.assertEquals(new BigDecimal("42"), result);
    }

    @Test
    void testNullHandling() throws SQLException {
        Mockito.lenient().when(mockResultSet.getBigDecimal(1)).thenReturn(null);
        Mockito.lenient().when(mockResultSet.wasNull()).thenReturn(true);

        Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.DECIMAL, true, Types.INTEGER);
        Assertions.assertNull(result);

        result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.DECIMAL, false, Types.INTEGER);
        Assertions.assertEquals(0, result);
    }

    @Test
	void testUnscaledType() throws SQLException {
		Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, true, Types.INTEGER);
		Assertions.assertNull(result);

		result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, false, Types.INTEGER);
		Assertions.assertInstanceOf(InputStream.class, result);
	}

    @Test
	void testUnsupportedType() throws SQLException {
		Object result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, true, Integer.MAX_VALUE - 1);
		Assertions.assertNull(result);

		result = SQLTypeConverter.getValueForType(mockResultSet, 1, Types.BLOB, false, Integer.MAX_VALUE - 1);
		Assertions.assertInstanceOf(InputStream.class, result);
	}
}
