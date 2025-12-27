package no.redeye.lib.jdax;

import org.junit.jupiter.api.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.mockito.Mockito;

public abstract class ConnectorContextTestBase {

//    private DataSource ds;
    protected Connection c1;
    protected Connection c2;

    @BeforeEach
    void setup() throws SQLException {
        DataSource ds = Mockito.mock(DataSource.class);
        c1 = Mockito.mock(Connection.class);
        c2 = Mockito.mock(Connection.class);

        Mockito.when(ds.getConnection()).thenReturn(c1, c2);

        Connector.register("db1", ds);
        Connector.register("db2", ds);
    }

    @AfterEach
    void cleanup() {
        Connector.detach("ctx");
        Mockito.reset(c1, c2);
//        TestConnector.DATASOURCES.clear();
    }

}
