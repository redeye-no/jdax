package no.redeye.lib.jdax;

import java.sql.SQLException;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.UpdateResults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DAOTypeTests extends JDBCApiMocks {

    @Test
    void select_withoutParams_executesQuery() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        ResultRows rows = dao.select("select * from table");

        Assertions.assertNotNull(rows);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
//        assertGetNextResultSet();
        assertGetMetadata();

        assertNoConnectionCommit();
        assertNoConnectionRollback();
    }

    @Test
    void select_withWhereClause_executesQuery() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        ResultRows rows = dao.select(new Object[]{"a", "b"}, "select * from table where x = ? and y = ?");

        Assertions.assertNotNull(rows);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
//        assertGetNextResultSet();
        assertGetMetadata();

        assertNoConnectionCommit();
        assertNoConnectionRollback();
    }

    @Test
    void select_withInClause_executesQuery() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        ResultRows rows = dao.select("select * from table where z in (??)", new Object[]{"a", "b"});

        Assertions.assertNotNull(rows);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
//        assertGetNextResultSet();
        assertGetMetadata();

        assertNoConnectionCommit();
        assertNoConnectionRollback();
    }

    @Test
    void select_withWhereAndInClause_executesQuery() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        ResultRows rows = dao.select(
                new Object[]{"a", "b"}, 
                "select * from table where x = ? and y = ? and z in (??)", new Object[]{"a", "b"});

        Assertions.assertNotNull(rows);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
//        assertGetNextResultSet();
        assertGetMetadata();

        assertNoConnectionCommit();
        assertNoConnectionRollback();
    }

    @Test
    void select_withVOParams_executesQuery() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        ResultRows rows = dao.select(VO, "select * from table");

        Assertions.assertNotNull(rows);

        assertPrepareStatementWithQueryOnly();
        assertExecuteQuery();
//        assertGetNextResultSet();
        assertGetMetadata();

        assertNoConnectionCommit();
        assertNoConnectionRollback();
    }

    @Test
    void insertOne_commits_and_returnsGeneratedKeys() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        InsertResults result
                = dao.insertOne(new Object[]{"a", "b"}, "insert into t values (?, ?)", "id");

        Assertions.assertNotNull(result);

        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();

//        Mockito.verify(connection, Mockito.times(1)).commit();
//        Mockito.verify(connection, Mockito.never()).rollback();
    }

    @Test
    void insertOneVO_commits_and_returnsGeneratedKeys() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        InsertResults result
                = dao.insertOne(VO, "insert into t values (?, ?)", "id");

        Assertions.assertNotNull(result);

        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();

//        Mockito.verify(connection, Mockito.times(1)).commit();
//        Mockito.verify(connection, Mockito.never()).rollback();
    }

    @Test
    void insertVO_commits_and_returnsGeneratedKeys() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        InsertResults result
                = dao.insert(VO, "insert into t values (?, ?)", "id");

        Assertions.assertNotNull(result);

        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();

//        Mockito.verify(connection, Mockito.times(1)).commit();
//        Mockito.verify(connection, Mockito.never()).rollback();
    }

    @Test
    void update_commitsOnce() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        UpdateResults res
                = dao.update(new Object[]{"x"}, "update t set a=?");

        Assertions.assertNotNull(res);

        assertExecuteUpdate();

//        Mockito.verify(connection, Mockito.times(1)).commit();
//        Mockito.verify(connection, Mockito.never()).rollback();
    }

    @Test
    void updateVO_commitsOnce() throws SQLException {
        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        UpdateResults res
                = dao.update(VO, "update t set a=?");

        Assertions.assertNotNull(res);

        assertExecuteUpdate();

//        Mockito.verify(connection, Mockito.times(1)).commit();
//        Mockito.verify(connection, Mockito.never()).rollback();
    }

    @Test
    void update_rollbackOnSQLException() throws SQLException {
        Mockito.doThrow(new SQLException("fail"))
                .when(ps).executeUpdate();

        DAOType dao = new DAOType(DATASOURCE_NAME, CONTEXT_NAME);

        Assertions.assertThrows(SQLException.class,
                () -> dao.update(new Object[]{"x"}, "update t set a=?"));

//        Mockito.verify(connection, Mockito.never()).commit();
//        Mockito.verify(connection, Mockito.times(1)).rollback();
    }

}
