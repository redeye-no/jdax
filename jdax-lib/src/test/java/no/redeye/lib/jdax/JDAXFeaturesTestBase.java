package no.redeye.lib.jdax;

import java.sql.SQLException;
import no.redeye.lib.jdax.sql.DBQueries;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.InsertResults;
import no.redeye.lib.jdax.types.VO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 */
public class JDAXFeaturesTestBase extends TestBase {

    private final String DS_NAME = "JDAXFeaturesTestBase";

    protected final AllTypesRecord TEST_RECORD_ALL_VALUES = new AllTypesRecord(
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

    protected final AllTypesRecord TEST_RECORD_NULL_VALUES = new AllTypesRecord(
            ID, INTEGER_VALUE,
            BIGINT_VALUE,
            REAL_VALUE,
            FLOAT_VALUE,
            DOUBLE_VALUE,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );

    protected final String INSERT_FULL_RECORD = """
        INSERT INTO TEST_TABLE
        (integerField, bigintField, realField, floatField, doubleField, decimalField, numericField, dateField, timeField, timestampField, charField, varcharField, blobField, clobField)
        VALUES (#, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    protected final String INSERT_NULLS_RECORD = """
        INSERT INTO TEST_TABLE
        (integerField, bigintField, realField, floatField, doubleField, decimalField, numericField, dateField, timeField, timestampField, charField, varcharField, blobField, clobField)
        VALUES (null, null, null, null, null, null, null, null, null, null, null, null, null, null)""";

    protected final String UPDATE_ALL_RECORDS = """
        UPDATE TEST_TABLE
        SET integerField = 127, bigintField = 127, realField = 127, floatField = 127, doubleField = 127""";

    protected final String SELECT_ALL_COLUMNS = "SELECT * FROM TEST_TABLE";
    protected final String SELECT_NUMERIC_COLUMNS = "SELECT integerField, bigintField, realField, floatField, doubleField, decimalField, numericField FROM TEST_TABLE";
    protected final String SELECT_TEMPORAL_COLUMNS = "SELECT dateField, timeField, timestampField FROM TEST_TABLE";

    protected InsertResults setUpTest(VO vo, String insertQuery, String tableName, String dsName) throws SQLException {
        return setUpTest(vo, insertQuery, tableName, "", dsName, DS_NAME);
    }

//    protected InsertResults setUpTest(VO vo, String insertQuery, String tableName, String fields, String dsName) throws SQLException {
//        setUpTypesTable(tableName, dsName);
//        return new DBQueries(dsName, DS_NAME).insertRow(vo, insertQuery, fields.split(","));
//    }

    protected InsertResults setUpTest(VO vo, String insertQuery, String tableName, String fields, String dsName, String cxName) throws SQLException {
        setUpTypesTable(tableName, dsName, cxName);
        return new DBQueries(dsName, cxName).insertRow(vo, insertQuery, fields.split(","));
    }

    protected String toTestQuery(String query, String tableName) {
        return query.replaceFirst("TEST_TABLE", tableName);
    }

    private ConnectorContext connector;

//    @BeforeAll
//    public void beforeAll() throws SQLException {
//        setUpDataSource(DS_NAME, Features.NULL_RESULTS_DISABLED);
//    }
    @BeforeEach
    public void beforeEach() throws SQLException {
        connector = Connector.context(DS_NAME);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        try (ConnectorContext cc = connector) {
        }
    }

    @AfterAll
    public void tearDown() throws SQLException {
        tearDownDS(DS_NAME);
    }
}
