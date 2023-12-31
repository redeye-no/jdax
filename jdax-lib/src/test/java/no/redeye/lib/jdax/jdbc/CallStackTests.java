package no.redeye.lib.jdax.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.Function;
import javax.sql.DataSource;
import no.redeye.lib.jdax.types.DVO;
import no.redeye.lib.jdax.types.DVODAO;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
public class CallStackTests {

    @Test
    public void whenInsertVOAndReturnIdentityFieldExpectUpdateAndGetGenerateKeys() throws SQLException {
        DVODAO dao = new DVODAO(DS_NAME);
        long id = dao.insertWithIdentityField(VO);

        assertPrepareStatementWithQueryAndFields();
        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();
    }

    @Test
    public void whenInsertVOAndReturnSequenceFieldExpectUpdateAndGetGenerateKeys() throws SQLException {
        DVODAO dao = new DVODAO(DS_NAME);
        long id = dao.insertWithSequenceField(VO);

        assertPrepareStatementWithQueryAndFields();
        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();
    }

    @Test
    public void whenInsertValuesAndReturnSequenceFieldExpectUpdateAndGetGenerateKeys() throws SQLException {
        DVODAO dao = new DVODAO(DS_NAME);
        Object[] values = new Object[]{"-1", "44", "four four"};
        ResultRows vos = dao.selectAllNamedFieldsForSomeRows(values);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
        assertGetMetadata();
    }

    @Test
    public void whenSimpleQueryReturnsExpectResultsetAndCallToNext() throws SQLException {
        DVODAO dao = new DVODAO(DS_NAME);
        ResultRows vos = dao.selectAllNamedFields();

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
        assertGetNextResultSet();
    }

    @Test
    public void whenConnectionIsNotUsedThenExpectNoCommit() throws SQLException {
        Connector.commit(DS_NAME);
        assertNoConnectionCommit();
    }

    @Test
    public void whenConnectionIsNotUsedThenExpectNoRollbacks() throws SQLException {
        Connector.rollback(DS_NAME);
        assertNoConnectionRollback();
    }

    private static final String DS_NAME = "ds";

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement ps;

    @Mock
    protected ResultSet rs; 

    @Mock
    private ResultSet keys; 

    @Mock
    private ResultSetMetaData metaData;

    private final DVO VO = new DVO("1", "911", "nine one one");

    @BeforeEach
    public void setUp() throws Exception {
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

        Mockito.lenient().when(metaData.getColumnCount()).thenReturn(3);
        Mockito.lenient().when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        Mockito.lenient().when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        Mockito.lenient().when(metaData.getColumnType(3)).thenReturn(Types.VARCHAR);

        Mockito.lenient().when(rs.getMetaData()).thenReturn(metaData);

        Mockito.lenient().when(rs.first()).thenReturn(true);

        Mockito.lenient().when(keys.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);

        Connector.prepare(DS_NAME, new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        }, Features.AUTO_COMMIT_ENABLED);

        Assertions.assertNotNull(dataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Connector.commit(DS_NAME);
        Connector.close(DS_NAME);
        Connector.remove(DS_NAME);
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

}
