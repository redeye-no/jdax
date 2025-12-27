package no.redeye.lib.jdax;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

final class TestConnector {

    static final Map<String, DataSource> DATASOURCES = new HashMap<>();

    static Connection openNew(String dsName) throws SQLException {
        DataSource ds = DATASOURCES.get(dsName);
        if (ds == null) {
            throw new IllegalStateException("No DataSource for " + dsName);
        }
        return ds.getConnection();
    }
}
