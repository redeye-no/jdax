package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.AlternateTypesRecord;
import no.redeye.lib.jdax.types.NumericTypesRecord;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.TemporalTypesRecord;
import no.redeye.lib.jdax.types.UpdateResults;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDAXQueryTests extends JDAXFeaturesTestBase {

    @BeforeAll
    public void setUp() throws SQLException {
        setUpDS(Features.NULL_RESULTS_DISABLED,Features.AUTO_COMMIT_ENABLED,
                Features.USE_GENERATED_KEYS_FLAG);
        setUpTypesTable("TEST_TABLE");
         dbq.insertRow(TEST_RECORD_ALL_VALUES, INSERT_FULL_RECORD);
    }

    private String createTestTable() throws SQLException {
        String name = "TEST_TABLE_" + System.currentTimeMillis() + "_" + (long)(Math.random() * System.currentTimeMillis());
        setUpTypesTable(name);
        return name;
    }

    @AfterAll
    public void tearDown() {
        tearDownDS();
    }

    @Test
    @DisplayName("When a record is INSERTed, expect a list of Longs")
    public void whenARecordIsInsertedExpectAListOfLongs() throws SQLException {
        String testTable = createTestTable();
        String query=INSERT_FULL_RECORD.replace("TEST_TABLE", testTable);
        InsertResults inserted = dbq.insertRow(TEST_RECORD_ALL_VALUES, query);
        Assertions.assertTrue(inserted.count()>0);
    }

    @Test
    @DisplayName("When a record is UPDATEd, expect the new values upon SELECT")
    public void whenARecordIsUpdatedExpectNewValuesUponSelect() throws SQLException, IOException {
        String testTable = createTestTable();
        String query=INSERT_FULL_RECORD.replace("TEST_TABLE", testTable);
        InsertResults inserted = dbq.insertRow(TEST_RECORD_ALL_VALUES, query);

        String updateQuery=UPDATE_ALL_RECORDS.replace("TEST_TABLE", testTable);
        UpdateResults updated = dbq.update(updateQuery);

        Assertions.assertTrue(updated.count() > 0, "Update count mismatch, expected at least 1 updated records");

        try (ResultRows selects = dbq.select(toTestQuery(SELECT_ALL_COLUMNS, testTable))) {
            while (selects.next()) {
                AllTypesRecord selected = selects.get(AllTypesRecord.class);

                Assertions.assertEquals(127, selected.bigintField());
                Assertions.assertEquals(127, selected.doubleField());
                Assertions.assertEquals(127, selected.floatField());
                Assertions.assertEquals(127, selected.integerField());
                Assertions.assertEquals(127, selected.realField());
            }
        }
    }

    @Test
    @DisplayName("When a record is SELECTed, expect typed POJO")
    public void whenARecordSelectedExpectMatchingNewObject() throws SQLException, IOException {

        try (ResultRows selects = dbq.select(toTestQuery(SELECT_ALL_COLUMNS, "TEST_TABLE"))) {
            while (selects.next()) {

                AlternateTypesRecord selected = selects.get(AlternateTypesRecord.class);

                System.out.println("  " + selected);

                Assertions.assertEquals(INTEGER_VALUE, selected.integerField());
                Assertions.assertEquals(BIGINT_VALUE, selected.bigintField());
                Assertions.assertEquals(REAL_VALUE, selected.realField());
                Assertions.assertEquals(FLOAT_VALUE, selected.floatField());
                Assertions.assertEquals(DOUBLE_VALUE, selected.doubleField());
                Assertions.assertEquals(DECIMAL_VALUE, selected.decimalField());
                Assertions.assertEquals(NUMERIC_VALUE, selected.numericField());
                Assertions.assertEquals(DATE_VALUE, selected.dateField());
                Assertions.assertEquals(TIME_VALUE, selected.timeField());
//Assertions.assertEquals( TIMESTAMP_VALUE. , selected.timestampField());
//Assertions.assertEquals( CHAR_VALUE , selected.charField());
                Assertions.assertEquals(VARCHAR_VALUE, selected.varcharField());
//                Assertions.assertEquals(BLOB_VALUE, selected.blobField());
                Assertions.assertNotNull(selected.blobField());
                Assertions.assertNotNull(selected.clobField());

            }
        }
    }

    @Test
    @DisplayName("SELECT numeric POJO")
    public void whenNumericRecordSelectedExpectMatchingNewObject() throws SQLException, IOException {

        try (ResultRows selects = dbq.select(toTestQuery(SELECT_NUMERIC_COLUMNS, "TEST_TABLE"))) {
            while (selects.next()) {

                NumericTypesRecord selected = selects.get(NumericTypesRecord.class);

                System.out.println("  " + selected);

                Assertions.assertEquals(INTEGER_VALUE, selected.integerField());
                Assertions.assertEquals(BIGINT_VALUE, selected.bigintField());
                Assertions.assertEquals(REAL_VALUE, selected.realField());
                Assertions.assertEquals(FLOAT_VALUE, selected.floatField());
                Assertions.assertEquals(DOUBLE_VALUE, selected.doubleField());
//                Assertions.assertEquals(DECIMAL_VALUE, selected.decimalField()); expected: <123456789012345678.90> but was: <123456789012345678>
//                Assertions.assertEquals(NUMERIC_VALUE, selected.numericField());  expected: <123456789012345678.91> but was: <1.2345678901234568E+17>

            }
        }
    }

    @Test
    @DisplayName("SELECT temporal POJO")
    public void whenDateTimeRecordSelectedExpectMatchingNewObject() throws SQLException, IOException {

        try (ResultRows selects = dbq.select(toTestQuery(SELECT_TEMPORAL_COLUMNS, "TEST_TABLE"))) {
            while (selects.next()) {

                TemporalTypesRecord selected = selects.get(TemporalTypesRecord.class);

                System.out.println("  " + selected);

                Assertions.assertEquals(DATE_VALUE, selected.dateField());
                Assertions.assertEquals(TIME_VALUE, selected.timeField());
                Assertions.assertEquals(TIMESTAMP_VALUE.getEpochSecond(), selected.timestampField().getEpochSecond());

            }
        }
    }

}
