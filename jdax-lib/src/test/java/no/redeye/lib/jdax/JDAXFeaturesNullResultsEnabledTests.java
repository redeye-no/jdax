package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class JDAXFeaturesNullResultsEnabledTests extends JDAXFeaturesTestBase {

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void whenNullResultsDisabledFlagIsNotSetThenReturnNullFields() throws SQLException, IOException {
        String tableName = "whenNullResultsDisabledFlagIsNotSetThenReturnNullFields";
        List<Long> identities = setUpTest(
                TEST_RECORD_NULL_VALUES,
                toTestTable(INSERT_NULLS_RECORD, tableName),
                tableName,
                Features.USE_GENERATED_KEYS_FLAG);

        Assertions.assertEquals(1, identities.size());

        try (ResultRows selects = dbq.select(toTestTable(SELECT_ALL_ROWS, tableName))) {
            while (selects.next()) {
                AllTypesRecord selected = selects.get(AllTypesRecord.class);

                Assertions.assertEquals(0l, selected.bigintField());
                Assertions.assertNull(selected.blobField());
                Assertions.assertNull(selected.charField());
                Assertions.assertNull(selected.clobField());
                Assertions.assertNull(selected.dateField());
                Assertions.assertNull(selected.decimalField());
                Assertions.assertEquals(0.0d, selected.doubleField());
                Assertions.assertEquals(0.0d, selected.floatField());
                Assertions.assertEquals(0, selected.integerField());
                Assertions.assertNull(selected.numericField());
                Assertions.assertEquals(0.0f, selected.realField());
                Assertions.assertNull(selected.timeField());
                Assertions.assertNull(selected.timestampField());
                Assertions.assertNull(selected.varcharField());
            }
        }
        tearDownDS();
    }

}
