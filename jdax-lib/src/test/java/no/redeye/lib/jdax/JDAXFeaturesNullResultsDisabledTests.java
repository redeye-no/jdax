package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import no.redeye.lib.jdax.sql.DBQueries;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class JDAXFeaturesNullResultsDisabledTests extends JDAXFeaturesTestBase {

    private String DS_NAME = "JDAXFeaturesNullResultsDisabledTests";
    private String CX_NAME = DS_NAME;
    private final String TEST_TABLE = "givenNullResultsDisabled_thenNullFieldsReturned";

    private void setUp() throws SQLException {
        setUpDataSource(DS_NAME, Features.NULL_RESULTS_DISABLED);
        Connection connection = Connector.connection(DS_NAME);
        InsertResults inserts = setUpTest(
                TEST_RECORD_NULL_VALUES,
                toTestQuery(INSERT_NULLS_RECORD, TEST_TABLE),
                TEST_TABLE,
                DS_NAME);

        Assertions.assertEquals(1, inserts.count());
    }

    @Test
    @DisplayName("Returns null fields when null results are disabled")
    public void givenNullResultsDisabled_thenNullFieldsReturned() throws SQLException, IOException {
        setUp();
        try (ConnectorContext context = Connector.context(CX_NAME);
                Connection connection = context.connection(DS_NAME);
                ResultRows selects = new DBQueries(DS_NAME, CX_NAME).select(toTestQuery(SELECT_ALL_COLUMNS, TEST_TABLE))) {
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
        tearDownDS(DS_NAME);
    }

}
