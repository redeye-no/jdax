package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class JDAXFeaturesNullResultsDisabledTests extends JDAXFeaturesTestBase {

    private final String TEST_TABLE = "givenNullResultsDisabled_thenNullFieldsReturned";

    @BeforeEach
    public void setUp() throws Exception {
        InsertResults inserts = setUpTest(
                TEST_RECORD_NULL_VALUES,
                toTestQuery(INSERT_NULLS_RECORD, TEST_TABLE),
                TEST_TABLE,
                Features.NULL_RESULTS_DISABLED);

        Assertions.assertEquals(1, inserts.count());
    }

    @AfterEach
    public void tearDown() {
        tearDownDS();
    }

    @Test
    @DisplayName("Returns null fields when null results are disabled")
    public void givenNullResultsDisabled_thenNullFieldsReturned() throws SQLException, IOException {
        try (ResultRows selects = dbq.select(toTestQuery(SELECT_ALL_COLUMNS, TEST_TABLE))) {
            while (selects.next()) {
                AllTypesRecord selected = selects.get(AllTypesRecord.class);

                Assertions.assertEquals(0l, selected.bigintField());
                Assertions.assertNotNull(selected.blobField());
                Assertions.assertNotNull(selected.charField());
                Assertions.assertNotNull(selected.clobField());
                Assertions.assertNotNull(selected.dateField());
                Assertions.assertNotNull(selected.decimalField());
                Assertions.assertEquals(0.0d, selected.doubleField());
                Assertions.assertEquals(0.0d, selected.floatField());
                Assertions.assertEquals(0, selected.integerField());
                Assertions.assertNotNull(selected.numericField());
                Assertions.assertEquals(0.0f, selected.realField());
                Assertions.assertNotNull(selected.timeField());
                Assertions.assertNotNull(selected.timestampField());
                Assertions.assertNotNull(selected.varcharField());
            }
        }
    }

}
