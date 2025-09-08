package no.redeye.lib.jdax;

import java.sql.SQLException;
import java.util.List;
import no.redeye.lib.jdax.sql.DBQueries;
import no.redeye.lib.jdax.types.AllTypesRecord;
import no.redeye.lib.jdax.types.VO;

/**
 */
public class JDAXFeaturesTestBase extends TestBase {

    private final String datasourceRef = "jdax-ds";

    protected final DBQueries dbq = new DBQueries(datasourceRef);

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

    protected List<Long> setUpTest(VO vo, String insert, String tableName, Features... featues) throws SQLException {
        setUpDS(featues);
        setUpTypesTable(tableName);
        return dbq.insertRow(vo, insert);
    }

    protected String toTestTable(String query, String tableName) {
        return query.replaceFirst("TEST_TABLE", tableName);
    }
}
