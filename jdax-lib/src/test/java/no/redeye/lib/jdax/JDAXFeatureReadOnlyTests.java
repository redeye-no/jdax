package no.redeye.lib.jdax;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class JDAXFeatureReadOnlyTests extends JDAXFeaturesTestBase {

    @Test
    public void whenReadOnlyModeFlagIsSetThenExpectExceptionOnWrites() {
        String tableName = "whenReadOnlyModeFlagIsSetThenExpectExceptionOnWrites";
        String dataSourceName = tableName;
        
        SQLException assertThrows = Assertions.assertThrows(SQLException.class, () -> {
            setUpDataSource(tableName, Features.READ_ONLY_MODE);

            try (ConnectorContext context = Connector.context("ctx");
                    Connection connection = context.connection(dataSourceName)) {

                setUpTest(
                        TEST_RECORD_NULL_VALUES,
                        toTestQuery(INSERT_NULLS_RECORD, tableName),
                        tableName,
                        tableName);
            }
            tearDownDS(tableName);
        });

        System.out.println("Thrown: " + assertThrows.getMessage());
        Assertions.assertNotNull(assertThrows);
    }

}
