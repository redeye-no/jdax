package no.redeye.lib.jdax.sql;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import no.redeye.lib.jdax.DAOType;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.VO;

/**
 *
 */
public class DBQueries {

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
        return dao.select((VO)null, query);
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
     
    private static final String INSERT_TEST_TYPE_0 = """
                                                       INSERT INTO TEST_TABLE (number10_0, number10_2, number19, number20, string, daten,timestampn ) 
                                                       VALUES (1010101010, 10101010.01, 1234567890123456789, 12345678901234567890, 'stringz',
                                                       	DATE '2017-11-14', 
                                                       	SYSTIMESTAMP 
                                                       )""";

    public List<Long> insertTestType_0() throws SQLException {
        return dao.insertOne(new Object[]{-1}, INSERT_TEST_TYPE_0);
    }

    private static final String INSERT_TEST_TYPE = """
                                                       INSERT INTO TEST_TABLE (number10_0, number10_2, number19, number20, string, daten, timestampn, blobu, klobu) 
                                                       VALUES (?,?,?,?,?,?,?,?,?)""";
    
    public List<Long> insertTestTypeGetDefaultID() throws SQLException {
        return dao.insertOne(new Object[]{1010101010, 10101010.01d, 1234567890123456789l, new BigDecimal("12345678901234567890"), "stringz", LocalDate.now().plusDays(7), Instant.now(), new byte[]{33, 34, 35, 36, 37}, "chichi ching ching"}, 
                INSERT_TEST_TYPE);
    }
    
    public List<Long> insertTestTypeGetNamedID() throws SQLException {
        return dao.insertOne(new Object[]{1010101010, 10101010.01d, 1234567890123456789l, new BigDecimal("12345678901234567890"), "stringz", LocalDate.now().plusDays(7), Instant.now(), new byte[]{33, 34, 35, 36, 37}, "chichi ching ching"}, 
                INSERT_TEST_TYPE,
                "number10", "number19");
//                "number10_0", "number19");
    }
    
    private static final String INSERT_VO_TYPE = """
                                                       INSERT INTO TEST_TABLE (number10_0, number10_2, number19, number20,string, blobu, klobu) 
                                                       VALUES (#,?,?,?,?,?,?,?)""";    
    public List<Long> insertTestVO(VO vo) throws SQLException {
        return dao.insertOne(vo, INSERT_VO_TYPE);
    }

    private static final String SELECT_SOME_TEST_TABLE = "SELECT number10, string, blobu FROM test_types ORDER BY timestampn ASC";

    public ResultRows selectSomeTestTypes() throws SQLException {
        return dao.select((VO)null, SELECT_SOME_TEST_TABLE);
//        return dao.select(new Object[]{}, SELECT_SOME_TEST_TABLE);
    }

    private static final String UPDATE_SOME_TEST_TABLE = "UPDATE test_types SET  number10_2 = ?, string = ? WHERE number10_0 = ?";

    public int update() throws SQLException {
        return dao.update(
                new Object[]{}, 
                UPDATE_SOME_TEST_TABLE);
    }
    
    private static final String UPDATE_WHERE = "UPDATE test_types SET  number10_2 = ?, string = ? WHERE number10_0 = ?";
    
    public int updateWhere() throws SQLException {
        return dao.update(
                new Object[]{ 2048, "" + LocalDate.now()}, 
                new Object[]{ 1024 }, 
                UPDATE_WHERE);
    }

    private static final String UPDATE_WHERE_INS = "UPDATE test_types SET  number10_2 = ?, string = ? WHERE number10_0 = ? AND number10_2 IN (??)";
    
    public int updateWhereIns() throws SQLException {
        return dao.update(
                new Object[]{ 2048, "" + LocalDate.now()}, 
                new Object[]{ 1024 }, 
                UPDATE_WHERE_INS, 
                new Object[]{ 10101010.01 });
    }
    
    
    
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
    
    private static final String _CREATE_TYPES_TABLE = """
        CREATE TABLE TEST_TABLE (
        id INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
        intfield INTEGER NOT NULL,
        bigintfield BIGINT NOT NULL,
        bigdecimalfield DECIMAL(20) NOT NULL,
        stringfield VARCHAR(20),
        datefield DATE,
        timestampfield TIMESTAMP,
        blobfield BLOB,
        clobfield CLOB
    )""";
    
    private static final String CREATE_SINGLETYPE_TABLE = """
        CREATE TABLE TYPES_TEST_TABLE (
        id INTEGER PRIMARY KEY,
        field COLUMN_TYPE
    )""";
    
    public int createSingleTypeTable(String typeName) throws SQLException {
        return dao.update(
                new Object[]{-1}, 
                CREATE_SINGLETYPE_TABLE
                        .replaceFirst("TYPES_TEST_TABLE", "T_" + typeName)
                        .replaceFirst("COLUMN_TYPE", typeName));
    }

    
    private static final String CREATE_MULTITYPES_TABLE = """
        CREATE TABLE TEST_TABLE (
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
        return dao.update(new Object[]{-1}, CREATE_MULTITYPES_TABLE.replaceFirst("TEST_TABLE", tableName));
    }
}
