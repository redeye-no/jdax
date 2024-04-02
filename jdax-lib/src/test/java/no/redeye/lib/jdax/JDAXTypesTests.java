package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    private static final String INSERT_TYPE = """
        INSERT INTO TEST_TABLE (id, field) 
        VALUES (?, ?)""";

    private static final String SELECT_TYPE = "SELECT * FROM TEST_TABLE WHERE id = ?";

    @BeforeAll
    public void beforeAll() throws SQLException {
        setUpDS();
    }

    @AfterAll
    public void tearDown() {
        tearDownDS();
    }

    private void createTypeTable(String typeName) throws SQLException {
        dbq.createSingleTypeTable(typeName);
    }

    private <T> T getTypeRow(int id, String typeName) throws SQLException, IOException {
        try (ResultRows selects = dbq.select(new Object[]{id}, SELECT_TYPE
                .replaceFirst("TEST_TABLE", "T_" + typeName))) {
            if (selects.next()) {
                return (T) selects.object("field");
            }
        }
        return null;
    }

    // Your existing method insertAndRetrieveTypeRecord
    private <T> T insertAndRetrieveTypeRecord(int id, T value, String typeName) throws SQLException, IOException {
        // Your logic to insert and retrieve record based on the given parameters
        createTypeTable(typeName);
        dbq.insertRow(new Object[]{id, value}, INSERT_TYPE
                .replaceFirst("TEST_TABLE", "T_" + typeName));
        return getTypeRow(id, typeName);
    }

    private <T> void testInsertAndRetrieveTypeRecord(Class<T> type, T in, String fieldType) throws SQLException, IOException {
        int id = ai.getAndIncrement();
        T retrievedValue = insertAndRetrieveTypeRecord(id, in, fieldType);
        Assertions.assertEquals(in, retrievedValue);
    }

    @Test
    public void booleanTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Boolean.class, false, "boolean");
    }

    @Test
    public void byteTest() throws SQLException, IOException {
//        testInsertAndRetrieveTypeRecord(byte[].class, new byte[]{3}, "tinyint");
    }

    @Test
    public void dateTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(LocalDate.class, LocalDate.EPOCH, "date");
    }

    @Test
    public void doubleTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Double.class, 1.23, "double");
    }

    @Test
    public void floatTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Float.class, 1.23f, "real");
    }

    @Test
    public void integerTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Integer.class, 123, "integer");
    }

    @Test
    public void shortTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(Short.class, (short) 123, "smallint");
    }

    @Test
    public void timeTest() throws SQLException, IOException {
        testInsertAndRetrieveTypeRecord(LocalTime.class, LocalTime.MIDNIGHT, "time");
    }

    @Test
    public void timestampTest() throws SQLException, IOException {
//        testInsertAndRetrieveTypeRecord(Instant.class, Instant.EPOCH, "timestamp");
    }
}
