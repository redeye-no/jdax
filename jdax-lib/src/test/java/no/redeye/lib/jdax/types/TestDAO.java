/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package no.redeye.lib.jdax.types;

import java.sql.SQLException;
import java.util.List;
import no.redeye.lib.jdax.DAOType;

/**
 *
 */
public class TestDAO {
    
    private final DAOType dao;

    public TestDAO(String n) {
        dao = new DAOType(n);
    }
        
    /*
    INSERT queries
    */
    public static final String ID_FIELD = "id";
    public static final String INSERT_W_IDENTITY = "insert into dvo (number, name) values (#, ?, ?)";

    public List<Long> insertWithIdentityField(VO vo) throws SQLException {
        return dao.insertOne(vo, INSERT_W_IDENTITY, ID_FIELD);
    }

    private static final String INSERT_W_SEQUENCE = "insert into dvo (id, number, name) values (#SEQ_TESTING.nextval, ?, ?)";

    public List<Long> insertWithSequenceField(VO vo) throws SQLException {
        return dao.insertOne(vo, INSERT_W_SEQUENCE, ID_FIELD);
    }

    private static final String INSERT_W_NEXT_VALUE = "insert into dvo (id, number, name) values (#next value for oseq, ?, ?)";

    public List<Long> insertAndGetCount(Object[] values) throws SQLException {
        return dao.insertOne(values, INSERT_W_NEXT_VALUE);
    }

    public List<Long> insertAndGetCount(VO vo) throws SQLException {
        return dao.insertOne(vo, INSERT_W_NEXT_VALUE);
    }
    
    /*
    SELECT queries
    */
    private static final String SELECT_ALL_NAMED_FIELDS = "select id, number, name from dvo";
    public ResultRows selectAllNamedFields() throws SQLException {
        return dao.select(SELECT_ALL_NAMED_FIELDS);
    }
    
    private static final String SELECT_ALL_NAMED_FIELDS_FOR_SOME_ROWS = "select id, number, name from dvo where ";
    public ResultRows selectAllNamedFieldsForSomeRows(Object[] values) throws SQLException {
        return dao.select(values, SELECT_ALL_NAMED_FIELDS_FOR_SOME_ROWS);
    }

    private static final String SELECT_IN_ODD = "select * from dvo where id in (??)";
    public ResultRows selectOddIDs(Object[][] ins) throws SQLException {
        return dao.select(SELECT_IN_ODD, ins);
    }

    private static final String SELECT_ONE_ID = "select * from dvo where id = ?";
    public ResultRows selectOneID(Object[] values) throws SQLException {
        return dao.select(values, SELECT_ONE_ID);
    }
    
    /*
    UPDATE queries
    */    
    private static final String UPDATE_ONE_ROW = "update dvo set name = ? where id = ?";
    public int updateOneRow(Object[] values, Object[] wheres) throws SQLException {
        return dao.update(values, wheres, UPDATE_ONE_ROW);
    }
    
}
