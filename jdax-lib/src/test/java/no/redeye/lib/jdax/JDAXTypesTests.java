package no.redeye.lib.jdax;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    
    @BeforeAll
    public void beforeAll() throws SQLException {
        setUpDS();
    }

    @AfterAll
    public void tearDown() {
        tearDownDS();
    }

    private void createTypeTable(Class<?> javaType) throws SQLException {
        dbq.createSingleTypeTable(javaType);
    }

    private static final Map<Class<?>, Integer> JAVA_TO_JDBC_TYPE = Map.ofEntries(
    Map.entry(BigDecimal.class, Types.DECIMAL),
    Map.entry(BigInteger.class, Types.NUMERIC),
    Map.entry(Boolean.class, Types.BOOLEAN),   // safer than BIT
    Map.entry(Double.class, Types.DOUBLE),
    Map.entry(Float.class, Types.REAL),
    Map.entry(Short.class, Types.SMALLINT),
    Map.entry(Integer.class, Types.INTEGER),
    Map.entry(Long.class, Types.BIGINT),
    Map.entry(String.class, Types.VARCHAR),

    // Java Time API
    Map.entry(Instant.class, Types.TIMESTAMP),
    Map.entry(LocalDate.class, Types.DATE),
    Map.entry(LocalTime.class, Types.TIME),
    Map.entry(LocalDateTime.class, Types.TIMESTAMP),

    // JDBC SQL types
    Map.entry(java.sql.Date.class, Types.DATE),
    Map.entry(java.sql.Time.class, Types.TIME),
    Map.entry(java.sql.Timestamp.class, Types.TIMESTAMP)
);
    
    private <T> T getTypeRow(int id, String tableName,Class<T> type) throws SQLException, IOException {
        String dml = String.format(SELECT_TYPE_TEMPLATE, tableName);

        try (ResultRows selects = dbq.select(new Object[]{id}, dml)) {

            if (selects.next()) {

                System.out.println("JAVA_TO_JDBC_TYPE -> "+JAVA_TO_JDBC_TYPE);
                System.out.println("selects.type " + type);
                System.out.println("selects.getString " + selects.getString("field"));
                System.out.println("selects.getObj.typ " + selects.getObject("field", JAVA_TO_JDBC_TYPE.get(type)));

                return (T) selects.getObject("field", JAVA_TO_JDBC_TYPE.get(type));
            }
        }
        return null;
    }

    private <T> T insertAndRetrieveTypeRecord(Class<T> type,Object value) throws SQLException, IOException {
        int id = ai.getAndIncrement();
        createTypeTable(type);

        String tableName = "T_" + type.getSimpleName();
        String dml = String.format(INSERT_TYPE_TEMPLATE, tableName);

        dbq.insertRow(new Object[]{id, value}, dml);
        return (T)getTypeRow(id, tableName,type);
    }

    private <T> void testInsertAndRetrieveTypeRecord(Class<T> type, Object in) throws SQLException, IOException {
        T retrievedValue = insertAndRetrieveTypeRecord(type, in);
        Assertions.assertEquals(in, retrievedValue);
    }

    @Test
    public void bigDecimalTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(BigDecimal.class, new BigDecimal("123"));
    }

    @Test
    public void bigIntegerTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(BigInteger.class, new BigDecimal("123"));
    }

    @Test
    public void booleanTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Boolean.class, false);
    }

    @Test
    public void byteTest() throws SQLException, IOException {
//        testInsertAndRetrieveTypeRecord(byte[].class, new byte[]{3});
    }

    @Test
    public void dateTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(LocalDate.class, LocalDate.EPOCH);
    }

    @Test
    public void doubleTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Double.class, 1.23);
    }

    @Test
    public void floatTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Float.class, 1.23f);
    }

    @Test
    public void integerTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Integer.class, 123);
    }

    @Test
    public void shortTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Short.class, (short) 123);
    }

    @Test
    public void timeTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(LocalTime.class, LocalTime.MIDNIGHT);
    }

    @Test
    public void timestampTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Instant.class, Instant.EPOCH.plusMillis(187));
    }
}
