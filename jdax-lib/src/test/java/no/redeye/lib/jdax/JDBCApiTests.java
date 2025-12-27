package no.redeye.lib.jdax;

import java.io.IOException;
import java.sql.SQLException;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.TestDAO;
import no.redeye.lib.jdax.types.ResultRows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class JDBCApiTests extends JDBCApiMocks {

    @Test
    public void whenInsertVOAndReturnIdentityFieldExpectUpdateAndGetGenerateKeys() throws SQLException {
        TestDAO dao = new TestDAO(DATASOURCE_NAME, CONTEXT_NAME);
        InsertResults id = dao.insertWithIdentityField(VO);

        assertPrepareStatementWithQueryAndFields();
        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();
        assertClosePreparedStatement();
    }

    @Test
    public void whenInsertVOAndReturnSequenceFieldExpectUpdateAndGetGenerateKeys() throws SQLException {
        TestDAO dao = new TestDAO(DATASOURCE_NAME, CONTEXT_NAME);
        InsertResults id = dao.insertWithSequenceField(VO);

        assertPrepareStatementWithQueryAndFields();
        assertExecuteUpdate();
        assertGetGeneratedKeys();
        assertGetNextKey();
        assertClosePreparedStatement();
    }

    @Test
    public void whenInsertValuesAndReturnSequenceFieldExpectUpdateAndGetGenerateKeys() throws SQLException, IOException {
        TestDAO dao = new TestDAO(DATASOURCE_NAME, CONTEXT_NAME);
        Object[] values = new Object[]{"-1", "44", "four four"};
        try (ResultRows results = dao.selectAllNamedFieldsForSomeRows(values)) {
            assertPrepareStatementWithQueryOnly();
            assertExecuteQuery();
            assertGetMetadata();
        }
        assertCloseResultSet();
    }

    @Test
    public void whenSimpleQueryWithResultsReturnsExpectResultsetAndCallToClose() throws SQLException, IOException {
        TestDAO dao = new TestDAO(DATASOURCE_NAME, CONTEXT_NAME);
        try (ResultRows results = dao.selectAllNamedFields()) {
            assertPrepareStatementWithQueryOnly();
            assertExecuteQuery();
        }
        assertCloseResultSet();
    }

    @Test
    public void whenSelectWithINsQueryReturnsExpectResultsetAndCallToClose() throws SQLException, IOException {
        TestDAO dao = new TestDAO(DATASOURCE_NAME, CONTEXT_NAME);
        Object[][] ins = new Object[][]{{"-1", "44", "four four"}};
        try (ResultRows results = dao.selectOddIDs(ins)) {
            assertPrepareStatementWithQueryOnly();
            assertExecuteQuery();
        }
        assertCloseResultSet();
    }

}
