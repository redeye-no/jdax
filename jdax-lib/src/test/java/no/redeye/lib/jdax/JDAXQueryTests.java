package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.ResultRows;
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
public class JDAXQueryTests extends JDAXFeaturesTestBase{

    @BeforeAll
    public void setUp() throws SQLException {
        setUpTest(TEST_RECORD_ALL_VALUES, INSERT_FULL_RECORD, "TEST_TABLE",
                Features.AUTO_COMMIT_ENABLED,
                Features.USE_GENERATED_KEYS_FLAG);
    }
    
    @AfterAll
    public void tearDown() {
        tearDownDS();
    }
    
    @Test
    @DisplayName("When a record is INSERTed, expect a list of Longs")
    public void whenARecordIsInsertedExpectAListOfLongs() throws SQLException {
        List<Long> inserted = dbq.insertRow(TEST_RECORD_ALL_VALUES, INSERT_FULL_RECORD);
        Assertions.assertFalse(inserted.isEmpty());
        Assertions.assertTrue(inserted.get(0) > 0);
    }
    
    @Test
    @DisplayName("When a record is UPDATEd, expect the new values upon SELECT")
    public void whenARecordIsUpdatedExpectNewValuesUponSelect() throws SQLException, IOException {
        whenARecordIsInsertedExpectAListOfLongs();
        whenARecordIsInsertedExpectAListOfLongs();
        
        int updated = dbq.update(UPDATE_ALL_RECORDS);

        Assertions.assertTrue(updated > 1, "Update count mismatch, expected at least 2 updated records");
        
        try (ResultRows selects = dbq.select(toTestTable(SELECT_ALL_ROWS, "TEST_TABLE"))) {
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

}
