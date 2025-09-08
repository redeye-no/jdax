package no.redeye.lib.jdax.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import no.redeye.lib.jdax.DAOType;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.VO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class DBQueries {

    protected static Logger logger = LogManager.getLogger("apiLogger");

    private final DAOType dao;

    public DBQueries(String n) {
        dao = new DAOType(n);
    }

    public List<Long> insertRow(Object[] row, String query) throws SQLException {
        return dao.insertOne(row, query);
    }

    public List<Long> insertRow(VO row, String query) throws SQLException {
        return dao.insertOne(row, query);
    }

    public ResultRows select(String query) throws SQLException {
        return dao.select((VO) null, query);
    }

    public ResultRows select(Object[] o, String query) throws SQLException {
        return dao.select(o, query);
    }

    public int update(String sql) throws SQLException {
        return dao.update(
                new Object[0],
                sql);
    }

    // --
    private static final Map<Class<?>, String> JAVA_TO_SQL_TYPE = Map.ofEntries(
            // Numbers
            Map.entry(BigDecimal.class, "DECIMAL(5, 1)"),
            Map.entry(BigInteger.class, "NUMERIC(5, 0)"),
            Map.entry(Double.class, "DOUBLE"),
            Map.entry(Float.class, "REAL"),
            Map.entry(Integer.class, "INTEGER"),
            Map.entry(Long.class, "BIGINT"),
            Map.entry(Short.class, "SMALLINT"),
            // Boolean
            Map.entry(Boolean.class, "BOOLEAN"),
            // Strings
            Map.entry(String.class, "VARCHAR(255)"),
            Map.entry(Character.class, "CHAR(8)"),
            Map.entry(Character[].class, "CHAR(8)"),
            // Binary
            Map.entry(Byte.class, "SMALLINT"),
            Map.entry(Byte[].class, "BLOB"), // Arrays are represented with [].class only for primitive arrays like byte[].class, not Byte[].class.
            // Legacy JDBC date/time types
            Map.entry(java.sql.Date.class, "DATE"),
            Map.entry(java.sql.Time.class, "TIME"),
            Map.entry(java.sql.Timestamp.class, "TIMESTAMP"),
            // java.time API
            Map.entry(LocalDate.class, "DATE"),
            Map.entry(LocalTime.class, "TIME"),
            Map.entry(LocalDateTime.class, "TIMESTAMP"),
            Map.entry(Instant.class, "TIMESTAMP")
    );

    private static final String CREATE_TEST_TABLE = """
        CREATE TABLE TEST_TABLE (
            number10 INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
            number10_0 INTEGER NOT NULL,
            number10_2 DECIMAL(10,2) NOT NULL,
            number19 BIGINT NOT NULL,
            number20 DECIMAL(20) NOT NULL,
            string VARCHAR(20),
            daten DATE,
            timestampn TIMESTAMP,
            blobu BLOB,
            klobu CLOB
        )""";

    public int createTestTables() throws SQLException {
        return dao.update(new Object[]{-1}, CREATE_TEST_TABLE);
    }

    private static final String CREATE_SINGLETYPE_TABLE_TEMPLATE = """
        CREATE TABLE %s (
        id INTEGER PRIMARY KEY,
        field %s
    )""";

    public String createSingleTypeTable(Class<?> javaType) throws SQLException {
        String sqlType = JAVA_TO_SQL_TYPE.get(javaType);
        if (null == sqlType) {
            throw new IllegalArgumentException("No SQL type mapping for " + javaType);
        }

        logger.info("DD type " + javaType + " -> " + sqlType);

        String typeName = javaType.getSimpleName().toUpperCase(Locale.ROOT) + "[";
        String tableName = "T_" + typeName.substring(0, typeName.indexOf("["));
        String ddl = String.format(CREATE_SINGLETYPE_TABLE_TEMPLATE, tableName, sqlType);

        dao.update(new Object[]{-1}, ddl);
        return tableName;
    }

    private static final String CREATE_MULTITYPES_TABLE_TEMPLATE = """
        CREATE TABLE %s (
        id INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
        integerField INT,
        bigintField BIGINT,
        realField REAL,
        floatField FLOAT,
        doubleField DOUBLE,
        decimalField DECIMAL(20,2),
        numericField NUMERIC(20,2),
        dateField DATE,
        timeField TIME,
        timestampField TIMESTAMP,
        charField CHAR(16),
        varcharField VARCHAR(255),
        blobField BLOB,
        clobField CLOB 
    )""";

    public int createMultiTypesTable(String tableName) throws SQLException {
        String ddl = String.format(CREATE_MULTITYPES_TABLE_TEMPLATE, tableName);
        return dao.update(new Object[]{-1}, ddl);
    }
}
