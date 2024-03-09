/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.redeye.lib.jdax.sql;

import java.io.IOException;
import no.redeye.lib.jdax.jdbc.Connector;
import no.redeye.lib.jdax.types.DVODAO;
import no.redeye.lib.jdax.jdbc.Features;
import java.sql.SQLException;
import java.util.function.Function;
import javax.sql.DataSource;
import no.redeye.lib.jdax.types.DVO;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.VO;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockFileDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 */
public class QueryTests {

    private final String datasourceRef = "ds";
    private final String MOCK_QUERYSETS = """
                                select id, number, name from dvo;
                                > id    number  name
                                > --    ------  ----
                                > 1     187     one eight seven
                                > 2     213     two one three
                                > 3     411     four one one
                                > 4     50      five o
                                > 5     1004    ten four
                                @ rows: 5

                                select * from dvo where id =?;
                                > id    number  name
                                > --    ------  ----
                                > 3     411     four one one
                                @ rows: 1

                                select * from dvo where id in (?,?,?);
                                > id    number  name
                                > --    ------  ----
                                > 1     187     one eight seven
                                > 3     411     four one one
                                > 5     1004    ten four
                                @ rows: 3

                                update dvo set name =? where id =?;
                                @ rows: 1

                                insert into dvo (id, number, name) values (next value for oseq, ?, ?);
                                @ rows: 1

                                # Insert new ow and return auto-generated identity value.
                                # This tests the param replacement feature.
                                insert into dvo (number, name) values (?, ?);
                                @ rows: 127

                                # Insert new ow and return sequence identity value.
                                # This tests the param replacement feature.
                                insert into dvo (id, number, name) values (SEQ_TESTING.nextval, ?, ?);
                                @ rows: 129

                                # Statement strings have no prefix and should be ended with a semi-colon
                                select 'A' from dual;
                                # Statements may be followed by results, using >
                                > A
                                > -
                                > A
                                # Statements should be followed by "@ rows: [N]" indicating the update count
                                @ rows: 1

                                """;

    @BeforeEach
    public void setUp() throws Exception {

        MockFileDatabase mdb = new MockFileDatabase(MOCK_QUERYSETS);
        MockConnection mcc = new MockConnection(mdb);

        DataSource dataSource = Mockito.mock(DataSource.class);

        Mockito.when(dataSource.getConnection()).thenReturn(mcc);
        Mockito.when(dataSource.getConnection(Mockito.anyString(), Mockito.anyString())).thenReturn(mcc);

        Connector.prepare(datasourceRef, new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        }, Features.AUTO_COMMIT_ENABLED);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Connector.close(datasourceRef);
        Connector.remove(datasourceRef);
    }

    @Test
    public void selectAllNamedFields() throws SQLException, IOException {
        DVODAO dao = new DVODAO(datasourceRef);
        try (ResultRows results = dao.selectAllNamedFields()) {
            assertResultRows(results);
        }
    }
    
    public record Thing(String id, String number, String name) implements VO {};
    
    private void assertResultRows(ResultRows results) throws SQLException {
        while (results.next()) {
            Thing get = results.get(Thing.class);
            System.out.println("Thing is " + get);
        }
    }

    @Test
    public void selectAllOddRowIDs() throws SQLException, IOException {
        DVODAO dao = new DVODAO(datasourceRef);
        try (ResultRows results = dao.selectOddIDs(null, new Object[][]{{1, 3, 7}})) {}
    }

    @Test
    public void selectOneID() throws SQLException, IOException {
        DVODAO dao = new DVODAO(datasourceRef);
        try (ResultRows vos = dao.selectOneID(new Object[]{3})){}
    }

    @Test
    public void updateOneRow() throws SQLException {
        DVODAO dao = new DVODAO(datasourceRef);
        int updated = dao.updateOneRow(new Object[]{"187"}, new Object[]{3});
        Assertions.assertEquals(1, updated);
    }

    @Test
    public void insertRawAndGetCount() throws SQLException {
        DVODAO dao = new DVODAO(datasourceRef);
        long inserted = dao.insertAndGetCount(new Object[]{"1", "191", "one nine one"});
        Assertions.assertEquals(1, inserted);
    }

    @Test
    public void insertVOAndGetCount() throws SQLException {
        DVODAO dao = new DVODAO(datasourceRef);
        DVO dvo = new DVO("1", "191", "one nine one");
        long updated = dao.insertAndGetCount(dvo);
        Assertions.assertEquals(1, updated);
    }

    @Test
    public void insertVOAndReturnIdentity() throws SQLException {
        DVODAO dao = new DVODAO(datasourceRef);
        DVO dvo = new DVO("1", "191", "one nine one");
        long updated = dao.insertWithIdentityField(dvo);
        Assertions.assertEquals(-1, updated); // -1, cos getGeneratedKeys  is not mocked in jooq with MockFileDatabase
    }

    @Test
    public void insertVOAndReturnSequence() throws SQLException {
        DVODAO dao = new DVODAO(datasourceRef);
        DVO dvo = new DVO("1", "191", "one nine one");
        long updated = dao.insertWithSequenceField(dvo);
        Assertions.assertEquals(-1, updated); // -1, cos getGeneratedKeys  is not mocked in jooq with MockFileDatabase
    }
}
