package no.redeye.lib.jdax;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class JDAXFeatureReadOnlyTests extends JDAXFeaturesTestBase {

    @AfterEach
    public void tearDown() {
        tearDownDS();
    }

    @Test
    public void whenReadOnlyModeFlagIsSetThenExpectExceptionOnWrites() {
        String tableName = "whenReadOnlyModeFlagIsSetThenExpectExceptionOnWrites";

        SQLException assertThrows = Assertions.assertThrows(SQLException.class, () -> {
            setUpTest(
                    TEST_RECORD_NULL_VALUES,
                    toTestQuery(INSERT_NULLS_RECORD, tableName),
                    tableName,
                    Features.READ_ONLY_MODE);
        });

        Assertions.assertNotNull(assertThrows);
    }

}
