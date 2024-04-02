package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class JDAXFeaturesNullResultsDisabledTests extends JDAXFeaturesTestBase {

    @Test
    public void whenNullResultsDisabledFlagIsSetThenReturnNoNullFields() throws SQLException, IOException {
        String tableName = "whenNullResultsDisabledFlagIsSetThenReturnNoNullFields";
        List<Long> identities = setUpTest(
                TEST_RECORD_NULL_VALUES,
                toTestTable(INSERT_NULLS_RECORD, tableName),
                tableName,
                Features.USE_GENERATED_KEYS_FLAG, Features.NULL_RESULTS_DISABLED);

        Assertions.assertEquals(1, identities.size());

        try (ResultRows selects = dbq.select(toTestTable(SELECT_ALL_ROWS, tableName))) {
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
        tearDownDS();
    }

}
