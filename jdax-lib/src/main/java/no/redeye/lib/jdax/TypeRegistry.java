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

    // Conversion registry: from -> (to -> converter)
    private static final Map<Integer, Map<Class<?>, Converter>> CONVERSIONS = new HashMap<>();

    // Map SQL types to canonical Java type
    public static final Map<Integer, Class<?>> SQL_TO_JAVA = Map.ofEntries(
            Map.entry(Types.BIGINT, Long.class),
            Map.entry(Types.BINARY, Byte[].class),
            Map.entry(Types.BIT, Boolean.class),
            Map.entry(Types.BLOB, InputStream.class),
            Map.entry(Types.BOOLEAN, Boolean.class),
            //            Map.entry(Types.CHAR, String.class),
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

    // Primitive <-> wrapper mapping
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
        /*
ARRAY			= 2003;
BIGINT			= -5;
BINARY			= -2;
BIT			= -7;
BLOB			= 2004;
BOOLEAN			= 16;
CHAR			= 1;
CLOB			= 2005;
DATALINK		= 70;
DATE			= 91;
DECIMAL			= 3;
DISTINCT		= 2001;
DOUBLE			= 8;
FLOAT			= 6;
INTEGER			= 4;
JAVA_OBJECT		= 2000;
LONGVARBINARY		= -4;
NULL			= 0;
NUMERIC			= 2;
NVARCHAR		= -9;
OTHER			= 1111;
REAL			= 7;
REF			= 2006;
SMALLINT		= 5;
STRUCT			= 2002;
TIME			= 92;
TIMESTAMP		= 93;
TINYINT			= -6;
VARBINARY		= -3;
VARCHAR			= 12;

JDBC 4.0
--------
LONGNVARCHAR		= -16;
NCHAR			= -15;
NCLOB			= 2011;
LONGVARCHAR		= -1;
ROWID			= -8;
SQLXML			= 2009;

JDBC 4.2
--------
REF_CURSOR		= 2012;
TIME_WITH_TIMEZONE	= 2013;
TIMESTAMP_WITH_TIMEZONE	= 2014;        
         */
        register(Types.DECIMAL, BigInteger.class, v -> ((BigDecimal) v).toBigInteger());
        register(Types.DECIMAL, Integer.class, v -> ((BigDecimal) v).intValue());
        register(Types.DECIMAL, Long.class, v -> ((BigDecimal) v).longValue());
        register(Types.DECIMAL, Double.class, v -> ((BigDecimal) v).doubleValue());
        register(Types.DECIMAL, Float.class, v -> ((BigDecimal) v).floatValue());
        register(Types.DECIMAL, Short.class, v -> ((BigDecimal) v).shortValue());
        register(Types.DECIMAL, String.class, v -> ((BigDecimal) v).toPlainString());
        register(Types.DECIMAL, BigDecimal.class, v -> (BigDecimal) v); // No convert
        register(Types.DECIMAL, Void.class, v -> BigDecimal.ZERO); // Null/void

        register(Types.NUMERIC, BigInteger.class, v -> ((BigDecimal) v).toBigInteger());
        register(Types.NUMERIC, Integer.class, v -> ((BigDecimal) v).intValue());
        register(Types.NUMERIC, Long.class, v -> ((BigDecimal) v).longValue());
        register(Types.NUMERIC, Double.class, v -> ((BigDecimal) v).doubleValue());
        register(Types.NUMERIC, Float.class, v -> ((BigDecimal) v).floatValue());
        register(Types.NUMERIC, Short.class, v -> ((BigDecimal) v).shortValue());
        register(Types.NUMERIC, String.class, v -> ((BigDecimal) v).toPlainString());
        register(Types.NUMERIC, BigDecimal.class, v -> (BigDecimal) v); // No convert
        register(Types.NUMERIC, Void.class, v -> BigDecimal.ZERO); // Null/void

        register(Types.BIGINT, Integer.class, v -> ((Long) v).intValue());
        register(Types.BIGINT, Double.class, v -> ((Long) v).doubleValue());
        register(Types.BIGINT, Float.class, v -> ((Long) v).floatValue());
        register(Types.BIGINT, Short.class, v -> ((Long) v).shortValue());
        register(Types.BIGINT, String.class, v -> ((Long) v).toString());
        register(Types.BIGINT, Long.class, v -> (Long) v); // No convert
        register(Types.BIGINT, Void.class, v -> 0l); // Null/void

        register(Types.INTEGER, Long.class, v -> ((Integer) v).longValue());
        register(Types.INTEGER, Double.class, v -> ((Integer) v).doubleValue());
        register(Types.INTEGER, Float.class, v -> ((Integer) v).floatValue());
        register(Types.INTEGER, Short.class, v -> ((Integer) v).shortValue());
        register(Types.INTEGER, Byte.class, v -> ((Integer) v).byteValue());
        register(Types.INTEGER, String.class, v -> ((Integer) v).toString());
        register(Types.INTEGER, Integer.class, v -> (Integer) v); // No convert
        register(Types.INTEGER, Void.class, v -> 0); // Null/void

        register(Types.DOUBLE, Integer.class, v -> ((Double) v).intValue());
        register(Types.DOUBLE, Long.class, v -> ((Double) v).longValue());
        register(Types.DOUBLE, Float.class, v -> ((Double) v).floatValue());
        register(Types.DOUBLE, Short.class, v -> ((Double) v).shortValue());
        register(Types.DOUBLE, String.class, v -> ((Double) v).toString());
        register(Types.DOUBLE, Double.class, v -> (Double) v); // No convert
        register(Types.DOUBLE, Void.class, v -> 0.0d); // Null/void

        // Float <--> Double
        register(Types.FLOAT, Integer.class, v -> ((Double) v).intValue());
        register(Types.FLOAT, Long.class, v -> ((Double) v).longValue());
        register(Types.FLOAT, Short.class, v -> ((Double) v).shortValue());
        register(Types.FLOAT, String.class, v -> ((Double) v).toString());
        register(Types.FLOAT, Double.class, v -> (Double) v);
        register(Types.FLOAT, Float.class, v -> ((Double) v).floatValue()); // No convert
        register(Types.FLOAT, Void.class, v -> 0.0d); // Null/void

        register(Types.REAL, Integer.class, v -> ((Float) v).intValue());
        register(Types.REAL, Long.class, v -> ((Float) v).longValue());
        register(Types.REAL, Double.class, v -> ((Float) v).doubleValue());
        register(Types.REAL, Short.class, v -> ((Float) v).shortValue());
        register(Types.REAL, String.class, v -> ((Float) v).toString());
        register(Types.REAL, Float.class, v -> (Float) v); // No convert
        register(Types.REAL, Void.class, v -> 0.0f); // Null/void

        register(Types.SMALLINT, Integer.class, v -> ((Short) v).intValue());
        register(Types.SMALLINT, Long.class, v -> ((Short) v).longValue());
        register(Types.SMALLINT, Double.class, v -> ((Short) v).doubleValue());
        register(Types.SMALLINT, Float.class, v -> ((Short) v).floatValue());
        register(Types.SMALLINT, Byte.class, v -> ((Short) v).byteValue());
        register(Types.SMALLINT, String.class, v -> ((Short) v).toString());
        register(Types.SMALLINT, Short.class, v -> (Short) v); // No convert
        register(Types.SMALLINT, Void.class, v -> 0); // Null/void

        register(Types.BOOLEAN, String.class, v -> ((Boolean) v) ? "true" : "false");
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
//
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
//        register(Types.BLOB, Blob.class, v ->  v); // No can do!
        register(Types.BLOB, Void.class, v -> new EmptyStream()); // Null/void

        // Readers
        register(Types.CLOB, Reader.class, v -> {
            if (null == v) {
                return new StringReader("");
            }
            return (Reader) v;
        });
//        register(Types.CLOB, Clob.class, v ->  v); // No can do
        register(Types.CLOB, Void.class, v -> new StringReader("")); // Null/void
    }

    private static void register(int from, Class<?> to, Converter fn) {
        CONVERSIONS.computeIfAbsent(from, k -> new HashMap<>()).put(to, fn);
    }

    // --- Public API ---
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
     * @param sourceType
     * @param targetType
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public static Object scaleToType(Object value, int sourceType, Class<?> targetType) throws SQLException {
        targetType = normalize(targetType);
        return convert(value, sourceType, targetType);
    }

    private static Object convert(Object value, int from, Class<?> targetType) throws SQLException {
        if (null == value) {
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
        if (type.isPrimitive()) {
            return PRIMITIVE_TO_WRAPPER.get(type);
        }

        return type;
    }
}
