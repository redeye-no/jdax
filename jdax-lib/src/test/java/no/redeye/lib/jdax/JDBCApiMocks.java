package no.redeye.lib.jdax;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Function;
import javax.sql.DataSource;
import no.redeye.lib.jdax.types.AllTypesRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 */
public abstract class JDBCApiMocks extends TestBase {

    protected final AllTypesRecord VO = new AllTypesRecord(
            ID, INTEGER_VALUE,
            BIGINT_VALUE,
            REAL_VALUE,
            FLOAT_VALUE,
            DOUBLE_VALUE,
            DECIMAL_VALUE,
            NUMERIC_VALUE,
            DATE_VALUE,
            TIME_VALUE,
            TIMESTAMP_VALUE,
            CHAR_VALUE,
            VARCHAR_VALUE,
            BLOB_VALUE,
            CLOB_VALUE
    );

    protected static final String DATASOURCE_NAME = "JDBCApiTests";
    protected static final String CONTEXT_NAME = DATASOURCE_NAME;

    private MockedStatic<Connector> connector;

    @Mock
    private ConnectorContext context;
    
    @Mock
    private DataSource dataSource;

    @Mock
    protected Connection connection;

    @Mock
    protected PreparedStatement ps;

    @Mock
    protected ResultSet rs;

    @Mock
    private ResultSet keys;

    @Mock
    private ResultSetMetaData metaData;

    @BeforeAll
    public void beforeAll() throws SQLException {
        setUpDataSource(DATASOURCE_NAME, Features.NULL_RESULTS_DISABLED);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        Mockito.lenient().when(dataSource.getConnection()).thenReturn(connection);

        Mockito.lenient().when(connection.createStatement()).thenReturn(ps);
        Mockito.lenient().when(connection.prepareStatement(Mockito.anyString())).thenReturn(ps);
        Mockito.lenient().when(connection.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(ps);
        Mockito.lenient().when(connection.prepareStatement(Mockito.anyString(), Mockito.any(String[].class))).thenReturn(ps);

        Mockito.lenient().when(rs.next()).thenReturn(true).thenReturn(false);

        Mockito.lenient().when(ps.executeQuery()).thenReturn(rs);
        Mockito.lenient().when(ps.executeUpdate()).thenReturn(1);

        Mockito.lenient().when(ps.getGeneratedKeys()).thenReturn(keys);

        Mockito.lenient().when(keys.next()).thenReturn(true);
        Mockito.lenient().when(keys.getMetaData()).thenReturn(metaData);

        Mockito.lenient().when(metaData.getColumnCount()).thenReturn(3);
        Mockito.lenient().when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.lenient().when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        Mockito.lenient().when(metaData.getColumnType(3)).thenReturn(Types.VARCHAR);

        Mockito.lenient().when(rs.getMetaData()).thenReturn(metaData);

        Mockito.lenient().when(rs.first()).thenReturn(true);

        Mockito.lenient().when(keys.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);

        Mockito.lenient().when(context.connection(DATASOURCE_NAME)).thenReturn(connection);
        
        connector = Mockito.mockStatic(Connector.class);

        connector
                .when(() -> Connector.context(Mockito.anyString()))
                .thenAnswer(inv -> context);

        connector
                .when(() -> Connector.has(Mockito.anyString()))
                .thenAnswer(inv -> true);

        Connector.register(DATASOURCE_NAME, new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        }, Features.AUTO_COMMIT_ENABLED);
//
//        context = Connector.context(CONTEXT_NAME);

        Assertions.assertNotNull(dataSource);
    }

    @AfterEach
    public void afterEach() throws Exception {
        try (ConnectorContext cc = context) {
        }
        Connector.detach(CONTEXT_NAME);
        connector.close();
    }

    @AfterAll
    public void afterAll() throws SQLException {
        tearDownDS(DATASOURCE_NAME);
    }

    protected void assertPrepareStatementWithQueryOnly() throws SQLException {
        Mockito.verify(connection, Mockito.atLeast(1)).prepareStatement(Mockito.anyString());
    }

    protected void assertPrepareStatementWithQueryAndFields() throws SQLException {
        Mockito.verify(connection, Mockito.atLeast(1)).prepareStatement(Mockito.anyString(), Mockito.any(String[].class));
    }

    protected void assertPrepareStatementWithQueryAndType() throws SQLException {
        Mockito.verify(connection, Mockito.atLeast(1)).prepareStatement(Mockito.anyString(), Mockito.anyInt());
    }

    protected void assertExecuteQuery() throws SQLException {
        Mockito.verify(ps, Mockito.times(1)).executeQuery();
    }

    protected void assertExecuteUpdate() throws SQLException {
        Mockito.verify(ps, Mockito.atLeast(1)).executeUpdate();
    }

    protected void assertGetGeneratedKeys() throws SQLException {
        Mockito.verify(ps, Mockito.times(1)).getGeneratedKeys();
    }

    protected void assertClosePreparedStatement() throws SQLException {
        Mockito.verify(ps, Mockito.times(1)).close();
    }

    protected void assertGetNextResultSet() throws SQLException {
        Mockito.verify(rs, Mockito.atLeast(1)).next();
    }

    protected void assertGetMetadata() throws SQLException {
        Mockito.verify(rs, Mockito.atLeast(1)).getMetaData();
    }

    protected void assertGetNextKey() throws SQLException {
        Mockito.verify(keys, Mockito.atLeast(1)).next();
    }

    protected void assertNoConnectionCommit() throws SQLException {
        Mockito.verify(connection, Mockito.times(0)).commit();
    }

    protected void assertNoConnectionRollback() throws SQLException {
        Mockito.verify(connection, Mockito.times(0)).rollback();
    }

    protected void assertCloseResultSet() throws SQLException {
        Mockito.verify(rs, Mockito.times(1)).close();
    }

}
