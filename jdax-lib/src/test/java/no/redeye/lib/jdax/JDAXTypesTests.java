package no.redeye.lib.jdax;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDAXTypesTests extends JDAXFeaturesTestBase {

    final AtomicInteger ai = new AtomicInteger(1024);

    private static final String INSERT_TYPE_TEMPLATE = """
        INSERT INTO %s (id, field)
        VALUES (?, ?)""";

    private static final String SELECT_TYPE_TEMPLATE = "SELECT * FROM %s WHERE id = ?";

    private static final String DROP_TYPE_TEMPLATE = "DROP TABLE %s";

    @BeforeAll
    public void beforeAll() throws SQLException {
        setUpDS(Features.NULL_RESULTS_DISABLED);
    }

    @AfterAll
    public void tearDown() {
        tearDownDS();
    }

    private String createTypeTable(Class<?> javaType) throws SQLException {
        return dbq.createSingleTypeTable(javaType);
    }

    private static final Map<Class<?>, Integer> JAVA_TO_JDBC_TYPE = new HashMap<>();

    static {
        // Invert the primitive-wrapper map
        for (var e : TypeRegistry.SQL_TO_JAVA.entrySet()) {
            JAVA_TO_JDBC_TYPE.put(e.getValue(), e.getKey());
        }
        JAVA_TO_JDBC_TYPE.put(BigInteger.class, Types.NUMERIC);
    }

    private <T> T getTypeRow(int id, String tableName, Class<T> type) throws SQLException, IOException {
        String dml = String.format(SELECT_TYPE_TEMPLATE, tableName);

        try (ResultRows selects = dbq.select(new Object[]{id}, dml)) {

            if (selects.next()) {
                logger.info("Java {} to JDBC type {}", type, JAVA_TO_JDBC_TYPE.get(type));
                return (T) selects.getObject("field", JAVA_TO_JDBC_TYPE.get(type));
            }
        } finally {
            dml = String.format(DROP_TYPE_TEMPLATE, tableName);
            dbq.update(dml);
        }
        return null;
    }

    private <T> InsertResults createTypeRecord(int id, String tableName, Class<T> type, Object value) throws SQLException, IOException {
        String dml = String.format(INSERT_TYPE_TEMPLATE, tableName);
        return dbq.insertRow(new Object[]{id, value}, dml);
    }

    private <T> T insertAndRetrieveTypeRecord(Class fieldType, Object value, Class<T> resultType) throws SQLException, IOException {
        int id = ai.getAndIncrement();
        String tableName = createTypeTable(fieldType);
        InsertResults created = createTypeRecord(id, tableName, fieldType, value);

        return (T) getTypeRow(id, tableName, resultType);
    }

    private <T> void testInsertAndRetrieveTypeRecord(Class fieldType, Object value, Class<T> resultType, Object expected) throws SQLException, IOException {
        logger.info("Test store and retrieve type conversions");

        T retrievedValue = insertAndRetrieveTypeRecord(fieldType, value, resultType);

        logger.info("- Field type = {}", fieldType);
        logger.info("- Value type = {}", value.getClass());
        logger.info("- Result type ={} ", resultType);

        // --
        if (retrievedValue instanceof byte[] actualValue) {
            byte[] expectedBytes;

            if (expected instanceof Byte[] boxed) {
                // Convert boxed Byte[] → primitive byte[]
                expectedBytes = new byte[boxed.length];
                for (int i = 0; i < boxed.length; i++) {
                    expectedBytes[i] = null != boxed[i] ? boxed[i] : 0;
                }
            } else if (expected instanceof byte[] prim) {
                expectedBytes = prim;
            } else {
                throw new AssertionError("Expected value is not a byte array: " + expected);
            }

            Assertions.assertArrayEquals(expectedBytes, actualValue, "Byte arrays do not match");
        } else if (retrievedValue instanceof Character[] actualChars) {
            Character[] expectedChars;

            if (expected instanceof char[] prim) {
                // Convert primitive char[] -> Character[]
                expectedChars = new Character[prim.length];
                for (int i = 0; i < prim.length; i++) {
                    expectedChars[i] = prim[i];
                }
            } else if (expected instanceof Character[] boxed) {
                expectedChars = boxed;
            } else if (expected instanceof String s) {
                // Convert String -> Character[]
                expectedChars = s.chars()
                        .mapToObj(c -> (char) c)
                        .toArray(Character[]::new);
            } else {
                throw new AssertionError("Expected value is not a Character array: " + expected);
            }

            Assertions.assertArrayEquals(expectedChars, actualChars, "Character arrays do not match");
        } else {
            Assertions.assertEquals(expected, retrievedValue);
        }
    }

    private <T> void testInsertAndRetrieveTypeRecord(Class<T> fieldType, Object value) throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(fieldType, value, fieldType, value);
    }

    @Test
    public void bigDecimalTest() throws SQLException, IOException {
        Object candidate = new BigDecimal("123.4");
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, BigDecimal.class, candidate);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, BigInteger.class, candidate);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, Double.class, 123.4d);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, Float.class, 123.4f);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(BigDecimal.class, candidate, String.class, "123.4");
    }

    @Test
    public void bigIntegerTest() throws SQLException, IOException {
        Object candidate = new BigInteger("123");
        Object expected = new BigDecimal("123"); // Because there is no BigInt in the DB
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, BigDecimal.class, expected);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, BigInteger.class, expected);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, Double.class, 123.0d);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, Float.class, 123.0f);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(BigInteger.class, candidate, String.class, "123");
    }

    @Test
    public void longTest() throws SQLException, IOException {
        Object candidate = Long.valueOf(123);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, Double.class, 123.0d);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, Float.class, 123.0f);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(Long.class, candidate, String.class, "123");
    }

    @Test
    public void integerTest() throws SQLException, IOException {
        Object candidate = 123;
        testInsertAndRetrieveTypeRecord(Integer.class, candidate);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, Integer.class, candidate);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, Double.class, 123.0d);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, Float.class, 123.0f);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(Integer.class, candidate, String.class, "123");
    }

    @Test
    public void doubleTest() throws SQLException, IOException {
        Object candidate = 123.4d;
        testInsertAndRetrieveTypeRecord(Double.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(Double.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(Double.class, candidate, Double.class, candidate);
        testInsertAndRetrieveTypeRecord(Double.class, candidate, Float.class, 123.4f);
        testInsertAndRetrieveTypeRecord(Double.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(Double.class, candidate, String.class, "123.4");

    }

    @Test
    public void floatTest() throws SQLException, IOException {
        // Float <--> Double
        Object candidate = 123.4f;
        Object expected = 123.4000015258789d; // floating-point precision trap
        testInsertAndRetrieveTypeRecord(Float.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(Float.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(Float.class, candidate, Double.class, expected);
        testInsertAndRetrieveTypeRecord(Float.class, candidate, Float.class, candidate);
        testInsertAndRetrieveTypeRecord(Float.class, candidate, Short.class, (short) 123);
        testInsertAndRetrieveTypeRecord(Float.class, candidate, String.class, "123.4");
    }

    @Test
    public void shortTest() throws SQLException, IOException {
        Object candidate = (short) 123;
        testInsertAndRetrieveTypeRecord(Short.class, candidate, Integer.class, 123);
        testInsertAndRetrieveTypeRecord(Short.class, candidate, Long.class, 123l);
        testInsertAndRetrieveTypeRecord(Short.class, candidate, Double.class, 123.0d);
        testInsertAndRetrieveTypeRecord(Short.class, candidate, Float.class, 123.0f);
        testInsertAndRetrieveTypeRecord(Short.class, candidate, Short.class, candidate);
        testInsertAndRetrieveTypeRecord(Short.class, candidate, String.class, "123");
    }

    @Test
    public void booleanTest() throws SQLException, IOException {
        Object candidate = Boolean.TRUE;
        testInsertAndRetrieveTypeRecord(Boolean.class, candidate, Boolean.class, candidate);
        testInsertAndRetrieveTypeRecord(Boolean.class, candidate, String.class, "true");
    }

    @Test
    public void dateTest() throws SQLException, IOException {
        Object candidate = LocalDate.now();
        testInsertAndRetrieveTypeRecord(LocalDate.class, candidate, LocalDate.class, candidate);
    }

    @Test
    public void timeTest() throws SQLException, IOException {
        Object candidate = LocalTime.now();
        Object expected = ((LocalTime) candidate).truncatedTo(ChronoUnit.SECONDS);
        testInsertAndRetrieveTypeRecord(LocalTime.class, candidate, LocalTime.class, expected);
    }

    @Test
    public void timestampTest() throws SQLException, IOException {
        Object candidate = Instant.now();
        testInsertAndRetrieveTypeRecord(Instant.class, candidate, Instant.class, candidate);
    }

    @Test
    public void stringTest() throws SQLException, IOException {
        Object candidate = "foo";
        testInsertAndRetrieveTypeRecord(String.class, candidate, String.class, candidate);
    }

    @Test
    public void charTest() throws SQLException, IOException {
        Object candidate = "f";
        Object expectedString = "f       "; // Test field is of type CHAR(8)
        Object expectedChars = ((String) expectedString).chars() // IntStream of code points
                .mapToObj(c -> (char) c)
                .toArray(Character[]::new);
        testInsertAndRetrieveTypeRecord(Character.class, candidate, String.class, expectedString);
        testInsertAndRetrieveTypeRecord(Character[].class, candidate, String.class, expectedString);
        testInsertAndRetrieveTypeRecord(Character[].class, candidate, Character[].class, expectedChars);
    }

    @Test
    public void byteTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Byte.class, (byte) 123);
    }

    @Test
    public void bytesTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Byte[].class, new Byte[]{3, 2, 1, 0});
    }
}
