package no.redeye.lib.jdax;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

class ConnectorTests {

    private static final String DS = "ds";
    private static final String CTX = "ctx";

    @AfterEach
    void cleanup() {
        Connector.deregister(DS);
        Connector.detach(CTX);
    }

    // -------------------------
    // register(Function)
    // -------------------------
    @Test
    void register_withFunction_registersDatasource() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Function<String, DataSource> fn = name -> ds;

        Connector.register(DS, fn);

        Assertions.assertTrue(Connector.ready(DS));
        Assertions.assertSame(ds, Connector.dataSource(DS));
    }

    @Test
    void register_withNullFunction_throwsSQLException() {
        Assertions.assertThrows(SQLException.class,
                () -> Connector.register(DS, (Function<String, DataSource>) null));
    }

    // -------------------------
    // register(DataSource)
    // -------------------------
    @Test
    void register_withDatasource_registersSuccessfully() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);

        Connector.register(DS, ds);

        Assertions.assertTrue(Connector.ready(DS));
        Assertions.assertSame(ds, Connector.dataSource(DS));
    }

    @Test
    void register_withNullDatasource_throwsSQLException() {
        Assertions.assertThrows(SQLException.class,
                () -> Connector.register(DS, (DataSource) null));
    }

    // -------------------------
    // deregister
    // -------------------------
    @Test
    void deregister_removesDatasource() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connector.register(DS, ds);

        Connector.deregister(DS);

        Assertions.assertFalse(Connector.ready(DS));
        Assertions.assertNull(Connector.dataSource(DS));
    }

    // -------------------------
    // enabled
    // -------------------------
    @Test
    void enabled_returnsTrueForEnabledFeature() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);

        Connector.register(DS, ds, Features.READ_ONLY_MODE);

        Assertions.assertTrue(Connector.enabled(DS, Features.READ_ONLY_MODE));
    }

    @Test
    void enabled_returnsFalseForMissingDatasource() {
        Assertions.assertFalse(Connector.enabled("missing", Features.READ_ONLY_MODE));
    }

    // -------------------------
    // ready
    // -------------------------
    @Test
    void ready_returnsTrueIfRegistered() throws Exception {
        Connector.register(DS, Mockito.mock(DataSource.class));
        Assertions.assertTrue(Connector.ready(DS));
    }

    // -------------------------
    // connection(String)
    // -------------------------
    @Test
    void connection_returnsConfiguredConnection() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);

        Mockito.when(ds.getConnection()).thenReturn(conn);

        Connector.register(
                DS,
                ds,
                Features.AUTO_COMMIT_ENABLED,
                Features.READ_ONLY_MODE
        );

        Connection result = Connector.connection(DS);

        Assertions.assertSame(conn, result);
        Mockito.verify(conn).setAutoCommit(true);
        Mockito.verify(conn).setReadOnly(true);
    }

    @Test
    void connection_unknownDatasource_throwsSQLException() {
        Assertions.assertThrows(SQLException.class,
                () -> Connector.connection("unknown"));
    }

    // -------------------------
    // connection(contextId, ds)
    // -------------------------
    @Test
    void connection_withContext_usesContextConnection() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);

        Mockito.when(ds.getConnection()).thenReturn(conn);

        Connector.register(DS, ds);

        ConnectorContext ctx = Connector.context(CTX);
        Connection c1 = ctx.connection(DS);
        Connection c2 = ctx.connection(DS);

        Assertions.assertSame(c1, c2); // same context-bound connection
    }

//    @Test
//    void connection_withoutContext_fallsBackToSingleConnection() throws Exception {
//        DataSource ds = Mockito.mock(DataSource.class);
//        Connection conn = Mockito.mock(Connection.class);
//
//        Mockito.when(ds.getConnection()).thenReturn(conn);
//        Connector.register(DS, ds);
//
//        Connection result = Connector.connection("missingCtx", DS);
//
//        Assertions.assertSame(conn, result);
//    }
    // -------------------------
    // peek
    // -------------------------
    @Test
    void has_returnsExistingContext() {
        ConnectorContext created = Connector.context("create_and_peek_returnSameContext");

        Assertions.assertNotNull(created);
        Assertions.assertTrue(Connector.has("create_and_peek_returnSameContext"));
    }

    @Test
    void has_without_returnsFalse() {
        Assertions.assertFalse(Connector.has("has_without_returnsFalse"));
    }
    
    // -------------------------
    // context
    // -------------------------
    @Test
    void context_createsAndCachesContext() {
        ConnectorContext c1 = Connector.context(CTX);
        ConnectorContext c2 = Connector.context(CTX);

        Assertions.assertSame(c1, c2);
    }

    // -------------------------
    // commit / rollback
    // -------------------------
    @Test
    void commit_commitsOpenConnections() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        ConnectorContext conc = Mockito.mock(ConnectorContext.class);

        Mockito.when(conc.connection(DS)).thenReturn(connection);
        Mockito.when(ds.getConnection()).thenReturn(connection);
        Mockito.when(connection.isClosed()).thenReturn(false);

        Connector.register(DS, ds);
        ConnectorContext cc = Connector.context(DS);
        cc.connection(DS);

        cc.commit();

        Mockito.verify(connection).commit();
            Mockito.verify(connection, Mockito.never()).rollback();
    }

    void commit_commitsOpenConnection() throws SQLException {
//        Connection connection = Mockito.mock(Connection.class);
//        Mockito.when(connection.isClosed()).thenReturn(false);
//
//        try (MockedStatic<Connector> connectorMock = Mockito.mockStatic(Connector.class)) {
//            connectorMock.when(() -> Connector.connection("commit_commitsOpenConnection"))
//                    .thenReturn(connection);

            ConnectorContext cc = Connector.context("commit_commitsOpenConnection");

            // IMPORTANT: open() creates and registers the connection
//            cc.connection("commit_commitsOpenConnection");
//
//            cc.commit();
//
//            Mockito.verify(connection).commit();
//            Mockito.verify(connection, Mockito.never()).rollback();
//        }
    }
//    @Test
//    void rollback_rollsBackOpenConnections() throws Exception {
//        DataSource ds = Mockito.mock(DataSource.class);
//        Connection conn = Mockito.mock(Connection.class);
//        ConnectorContext context = Mockito.mock(ConnectorContext.class);
//
//        Mockito.when(context.connection(DS)).thenReturn(conn);
//        Mockito.when(ds.getConnection()).thenReturn(conn);
//        Mockito.when(conn.isClosed()).thenReturn(false);
//
//        try (MockedStatic<Connector> connectorMock = Mockito.mockStatic(Connector.class)) {
//            connectorMock.when(() -> Connector.context(DS))
//                    .thenReturn(context);
//
//            Connector.register(DS, ds);
//            ConnectorContext cc = Connector.context(DS);
//            cc.connection(DS);
//
//            cc.rollback();
//
//            Mockito.verify(conn).rollback();
//        }
//    }

    // -------------------------
    // detach
    // -------------------------
    @Test
    void detach_closesAndRemovesContext() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);

        Mockito.when(ds.getConnection()).thenReturn(conn);

        Connector.register(DS, ds);
        ConnectorContext cc = Connector.context(DS);

        Connector.detach(CTX);

        Assertions.assertFalse(Connector.has(CTX));
    }
}
