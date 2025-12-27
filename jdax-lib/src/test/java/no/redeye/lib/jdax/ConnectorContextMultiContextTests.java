package no.redeye.lib.jdax;

import org.junit.jupiter.api.*;
import java.sql.Connection;

import org.mockito.Mockito;

public class ConnectorContextMultiContextTests extends ConnectorContextTestBase {

    @Test
    void multipleContexts_areIsolated() throws Exception {
        try (
                ConnectorContext req = Connector.context("request");
                ConnectorContext batch = Connector.context("batch")) {

            Connection r1 = req.connection("db1");
            Connection r2 = req.connection("db1");
            Connection b1 = batch.connection("db2");

            Assertions.assertSame(r1, c1); // ds1 mock returns
            Assertions.assertSame(r1, r2);
            Assertions.assertSame(b1, c2); // ds2 mock returns
            Assertions.assertNotSame(c1, c2);
        }
        Mockito.verify(c1).close(); // req closed))
        Mockito.verify(c2).close(); // batch closed
    }

}
