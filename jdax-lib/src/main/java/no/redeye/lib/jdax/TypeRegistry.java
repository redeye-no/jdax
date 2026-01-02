package no.redeye.lib.jdax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import no.redeye.lib.jdax.types.EmptyStream;

/**
 *
 */
public class TypeRegistry {

    @FunctionalInterface
    public interface Converter {

        Object apply(Object value) throws SQLException;
    }

    // Conversion registry
    private static final Map<Integer, Map<Class<?>, Converter>> CONVERSIONS = new HashMap<>();

    // Map SQL types to canonical Java type
    public static final Map<Integer, Class<?>> SQL_TO_JAVA = Map.ofEntries(
            Map.entry(Types.BIGINT, Long.class),
            Map.entry(Types.BINARY, Byte[].class),
            Map.entry(Types.BIT, Boolean.class),
            Map.entry(Types.BLOB, InputStream.class),
            Map.entry(Types.BOOLEAN, Boolean.class),
            Map.entry(Types.CHAR, Character[].class),
            Map.entry(Types.CLOB, Reader.class),
            Map.entry(Types.DATE, java.time.LocalDate.class),
            Map.entry(Types.DECIMAL, BigDecimal.class),
            Map.entry(Types.DOUBLE, Double.class),
            Map.entry(Types.FLOAT, Double.class),
            Map.entry(Types.INTEGER, Integer.class),
            Map.entry(Types.LONGNVARCHAR, String.class),
            Map.entry(Types.LONGVARBINARY, Byte[].class),
            Map.entry(Types.NCHAR, String.class),
            Map.entry(Types.NCLOB, Reader.class),
            Map.entry(Types.NVARCHAR, String.class),
            Map.entry(Types.NUMERIC, BigDecimal.class),
            Map.entry(Types.REAL, Float.class),
            Map.entry(Types.SMALLINT, Short.class),
            Map.entry(Types.TIME, LocalTime.class),
            Map.entry(Types.TIME_WITH_TIMEZONE, OffsetTime.class),
            Map.entry(Types.TIMESTAMP, Instant.class),
            Map.entry(Types.TIMESTAMP_WITH_TIMEZONE, Instant.class),
            Map.entry(Types.TINYINT, Byte.class),
            Map.entry(Types.VARBINARY, Byte[].class),
            Map.entry(Types.VARCHAR, String.class)
    );

    // Primitive -> wrapper mapping
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.ofEntries(
            Map.entry(boolean.class, Boolean.class),
            Map.entry(byte.class, Byte.class),
            Map.entry(short.class, Short.class),
            Map.entry(int.class, Integer.class),
            Map.entry(long.class, Long.class),
            Map.entry(float.class, Float.class),
            Map.entry(double.class, Double.class),
            Map.entry(char.class, Character.class)
    );

    static {
// Conversions register for SQL->Java types.
        register(Types.DECIMAL, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.DECIMAL, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.DECIMAL, Integer.class, ResultConverter::toInteger);
        register(Types.DECIMAL, Long.class, ResultConverter::toLong);
        register(Types.DECIMAL, Double.class, ResultConverter::toDouble);
        register(Types.DECIMAL, Float.class, ResultConverter::toFloat);
        register(Types.DECIMAL, Short.class, ResultConverter::toShort);
        register(Types.DECIMAL, String.class, v -> ((BigDecimal) v).toPlainString());
        register(Types.DECIMAL, BigDecimal.class, v -> (BigDecimal) v); // No convert
        register(Types.DECIMAL, Void.class, v -> BigDecimal.ZERO); // Null/void

        register(Types.NUMERIC, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.NUMERIC, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.NUMERIC, Integer.class, ResultConverter::toInteger);
        register(Types.NUMERIC, Long.class, ResultConverter::toLong);
        register(Types.NUMERIC, Double.class, ResultConverter::toDouble);
        register(Types.NUMERIC, Float.class, ResultConverter::toFloat);
        register(Types.NUMERIC, Short.class, ResultConverter::toShort);
        register(Types.NUMERIC, String.class, v -> ((BigDecimal) v).toPlainString());
        register(Types.NUMERIC, BigDecimal.class, v -> (BigDecimal) v); // No convert
        register(Types.NUMERIC, Void.class, v -> BigDecimal.ZERO); // Null/void

        register(Types.BIGINT, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.BIGINT, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.BIGINT, Integer.class, ResultConverter::toInteger);
        register(Types.BIGINT, Double.class, ResultConverter::toDouble);
        register(Types.BIGINT, Float.class, ResultConverter::toFloat);
        register(Types.BIGINT, Short.class, ResultConverter::toShort);
        register(Types.BIGINT, Byte.class, ResultConverter::toByte);
        register(Types.BIGINT, String.class, ResultConverter::toStringValue);
        register(Types.BIGINT, Long.class, v -> (Long) v); // No convert
        register(Types.BIGINT, Void.class, v -> 0l); // Null/void

        register(Types.INTEGER, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.INTEGER, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.INTEGER, Long.class, ResultConverter::toLong);
        register(Types.INTEGER, Double.class, ResultConverter::toDouble);
        register(Types.INTEGER, Float.class, ResultConverter::toFloat);
        register(Types.INTEGER, Short.class, ResultConverter::toShort);
        register(Types.INTEGER, Byte.class, ResultConverter::toByte);
        register(Types.INTEGER, String.class, ResultConverter::toStringValue);
        register(Types.INTEGER, Integer.class, v -> (Integer) v); // No convert
        register(Types.INTEGER, Void.class, v -> 0); // Null/void

        register(Types.DOUBLE, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.DOUBLE, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.DOUBLE, Integer.class, ResultConverter::toInteger);
        register(Types.DOUBLE, Long.class, ResultConverter::toLong);
        register(Types.DOUBLE, Float.class, ResultConverter::toFloat);
        register(Types.DOUBLE, Short.class, ResultConverter::toShort);
        register(Types.DOUBLE, Byte.class, ResultConverter::toByte);
        register(Types.DOUBLE, String.class, ResultConverter::toStringValue);
        register(Types.DOUBLE, Double.class, v -> (Double) v); // No convert
        register(Types.DOUBLE, Void.class, v -> 0.0d); // Null/void

// Float <--> Double
        register(Types.FLOAT, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.FLOAT, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.FLOAT, Integer.class, ResultConverter::toInteger);
        register(Types.FLOAT, Long.class, ResultConverter::toLong);
        register(Types.FLOAT, Short.class, ResultConverter::toShort);
        register(Types.FLOAT, Byte.class, ResultConverter::toByte);
        register(Types.FLOAT, String.class, ResultConverter::toStringValue);
        register(Types.FLOAT, Double.class, v -> (Double) v);
        register(Types.FLOAT, Float.class, ResultConverter::toFloat); // No convert
        register(Types.FLOAT, Void.class, v -> 0.0d); // Null/void

        register(Types.REAL, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.REAL, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.REAL, Integer.class, ResultConverter::toInteger);
        register(Types.REAL, Long.class, ResultConverter::toLong);
        register(Types.REAL, Double.class, ResultConverter::toDouble);
        register(Types.REAL, Short.class, ResultConverter::toShort);
        register(Types.REAL, Byte.class, ResultConverter::toByte);
        register(Types.REAL, String.class, ResultConverter::toStringValue);
        register(Types.REAL, Float.class, v -> (Float) v); // No convert
        register(Types.REAL, Void.class, v -> 0.0f); // Null/void

        register(Types.SMALLINT, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.SMALLINT, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.SMALLINT, Integer.class, ResultConverter::toInteger);
        register(Types.SMALLINT, Long.class, ResultConverter::toLong);
        register(Types.SMALLINT, Double.class, ResultConverter::toDouble);
        register(Types.SMALLINT, Float.class, ResultConverter::toFloat);
        register(Types.SMALLINT, Byte.class, ResultConverter::toByte);
        register(Types.SMALLINT, String.class, ResultConverter::toStringValue);
        register(Types.SMALLINT, Short.class, v -> (Short) v); // No convert
        register(Types.SMALLINT, Void.class, v -> 0); // Null/void

        register(Types.TINYINT, BigDecimal.class, ResultConverter::toBigDecimal);
        register(Types.TINYINT, BigInteger.class, ResultConverter::toBigInteger);
        register(Types.TINYINT, Integer.class, ResultConverter::toInteger);
        register(Types.TINYINT, Long.class, ResultConverter::toLong);
        register(Types.TINYINT, Double.class, ResultConverter::toDouble);
        register(Types.TINYINT, Float.class, ResultConverter::toFloat);
        register(Types.TINYINT, Byte.class, v -> (Byte) v);
        register(Types.TINYINT, String.class, ResultConverter::toStringValue);
        register(Types.TINYINT, Short.class, ResultConverter::toShort); // No convert
        register(Types.TINYINT, Void.class, v -> 0); // Null/void

        register(Types.BOOLEAN, String.class, ResultConverter::toStringValue);
        register(Types.BOOLEAN, Integer.class, v -> ((Boolean) v) ? 1 : 0);
        register(Types.BOOLEAN, Boolean.class, v -> (Boolean) v); // No convert
        register(Types.BOOLEAN, Void.class, v -> Boolean.FALSE); // Null/void

        // Date
        register(Types.DATE, LocalDate.class, v -> (LocalDate) v);
        register(Types.DATE, Void.class, v -> LocalDate.EPOCH); // Null/void

        // Time
        register(Types.TIME, LocalTime.class, v -> (LocalTime) v);
        register(Types.TIME, Void.class, v -> LocalTime.MIDNIGHT); // Null/void

        // Timestamp
        register(Types.TIMESTAMP, Long.class, v -> ((Instant) v).toEpochMilli());
        register(Types.TIMESTAMP, Instant.class, v -> (Instant) v); // No convert
        register(Types.TIMESTAMP, Void.class, v -> Instant.EPOCH); // Null/void

        // String
        register(Types.VARCHAR, String.class, v -> (String) v); // No convert
        register(Types.VARCHAR, Void.class, v -> ""); // Null/void
        register(Types.CHAR, String.class, v -> Arrays.stream((Character[]) v)
                .map(String::valueOf)
                .collect(Collectors.joining())); // No convert
        register(Types.CHAR, Character[].class, v -> (Character[]) v); // No convert
        register(Types.CHAR, Void.class, v -> ""); // Null/void

        // Binary
        register(Types.BINARY, BigInteger.class, v -> new BigInteger((byte[]) v));
        register(Types.BINARY, Byte.class, v -> (byte[]) v);
        register(Types.BINARY, Void.class, v -> "");

        // Streams
        register(Types.BLOB, InputStream.class, v -> {
            if (null == v) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return (InputStream) v;
        });
        register(Types.BLOB, Byte[].class, v -> {
            try (InputStream stream = (InputStream) v) {
                return stream.readAllBytes();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        register(Types.BLOB, Void.class, v -> new EmptyStream()); // Null/void

        // Readers
        register(Types.CLOB, Reader.class, v -> {
            if (null == v) {
                return new StringReader("");
            }
            return (Reader) v;
        });
        register(Types.CLOB, Void.class, v -> new StringReader("")); // Null/void

        // Unsupported types
        register(Types.ARRAY, Void.class, v -> v);
        register(Types.DATALINK, Void.class, v -> v);
        register(Types.DISTINCT, Void.class, v -> v);
        register(Types.JAVA_OBJECT, Void.class, v -> v);
        register(Types.NULL, Void.class, v -> v);
        register(Types.OTHER, Void.class, v -> v);
        register(Types.REF, Void.class, v -> v);
        register(Types.STRUCT, Void.class, v -> v);

        // Unsupported types JDBC 4.0
        register(Types.ROWID, Void.class, v -> v);
        register(Types.SQLXML, Void.class, v -> v);

        // Unsupported types JDBC 4.2
        register(Types.REF_CURSOR, Void.class, v -> v);
    }

    private static void register(int from, Class<?> to, Converter fn) {
        CONVERSIONS.computeIfAbsent(from, k -> new HashMap<>()).put(to, fn);
    }

    /**
     * Check if a value of type 'from' can be assigned/converted to 'to'
     *
     * @param from
     * @param to
     *
     * @return
     */
    public static boolean isCompatible(int from, Class<?> to) {
        Class<?> targetType = normalize(to);
        return CONVERSIONS.getOrDefault(from, Map.of()).containsKey(targetType);
    }

    /**
     * Convert value to requested type
     *
     * @param value
     * @param from
     * @param to
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public static Object scaleToType(Object value, int from, Class<?> to) throws SQLException {
        Class<?> targetType = normalize(to);

        if (null == value) {
            // For null values, force Void.class
            // to trigger "default for null"
            targetType = Void.class;
        }

        Converter fn = CONVERSIONS
                .getOrDefault(from, Map.of())
                .get(targetType);

        if (null != fn) {
            return fn.apply(value);
        }

        throw new IllegalArgumentException("No conversion from " + from + " to " + targetType);
    }

    private static Class<?> normalize(Class<?> type) {
        if (null == type) {
            type = Void.class;
        }
        if (type.isPrimitive()) {
            return PRIMITIVE_TO_WRAPPER.get(type);
        }

        return type;
    }
}
