package no.redeye.lib.jdax;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;
import javax.sql.DataSource;
import no.redeye.lib.jdax.sql.DBQueries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 */
public class TestBase {

    protected static Logger logger = LogManager.getLogger("apiLogger");

    protected final String BASE_DATASOURCE_NAME = "jdax-ds";
    protected final String BASE_CONTEXT_NAME = BASE_DATASOURCE_NAME;
    private final DBQueries dbq = new DBQueries(BASE_DATASOURCE_NAME, BASE_CONTEXT_NAME);

    protected final int ID = 1;
    protected final int INTEGER_VALUE = 1010101010;
    protected final long BIGINT_VALUE = 1234567890123456789l;
    protected final float REAL_VALUE = 1234567890123456789.0f;
    protected final double FLOAT_VALUE = 1234567890123456789.1d;
    protected final double DOUBLE_VALUE = 1234567890123456789.2d;
    protected final BigDecimal DECIMAL_VALUE = new BigDecimal("123456789012345678.90");
    protected final BigDecimal NUMERIC_VALUE = new BigDecimal("123456789012345678.91");
    protected final LocalDate DATE_VALUE = LocalDate.now().plusDays(7);
    protected final LocalTime TIME_VALUE = LocalTime.MIN;
    protected final Instant TIMESTAMP_VALUE = Instant.now();
    protected final String CHAR_VALUE = "char";
    protected final String VARCHAR_VALUE = "string";
    protected final InputStream BLOB_VALUE = new ByteArrayInputStream(new byte[0]);
    protected final Reader CLOB_VALUE = new StringReader("");

    static {
        System.setProperty("derby.stream.error.file", "target/derby.log");
    }

    protected void setUpDataSource(String dsName, Features... features) throws SQLException {
        logger.info("Set up datasource features, {}", features);
        initDS(dsName, features);
        Connector.context(BASE_CONTEXT_NAME);
    }

    protected void setUpTestTables(String dsName, String cxName) throws SQLException {
        logger.info("Set up test tables");
        new DBQueries(dsName, cxName).createTestTables();
        d("setUpTestTables", dsName);
    }

    protected void setUpTypesTable(String tableName, String dsName, String cxName) throws SQLException {
        logger.info("Set up test table {}", tableName);
        new DBQueries(dsName, cxName).createMultiTypesTable(tableName);
        d("setUpTypesTable " + tableName, dsName);
    }

    protected void tearDownDS(String dsName) {
        logger.info("Tear down datasource {}", BASE_DATASOURCE_NAME);
        d("tearDownDS", dsName);
//        Connector.close(DATASOURCE_NAME);
//        Connector.remove(DATASOURCE_NAME);

//        try(ConnectorContext cc=CurrentContext.get()){} catch (SQLException ex) {
//            System.getLogger(TestBase.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
//        }
    }

    public static String clob(Reader reader) throws SQLException {
        StringBuilder sb = new StringBuilder();
        if (null == reader) {
            return "null";
        }

        try (reader;
                BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
        return sb.toString();
    }

    private synchronized void initDS(String dsName, Features... features) throws SQLException {
        DataSource dataSource = dataSource();

        Function<String, DataSource> dsCreator = new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        };

        Connector.register(dsName, dsCreator, features);
        d("initDS", dsName);
    }

    private synchronized DataSource dataSource() {
        return new HikariDataSource(config());
    }

    private HikariConfig config() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");

        config.setAutoCommit(false);
        config.setInitializationFailTimeout(5000L);
        config.setLeakDetectionThreshold(12000L);
        config.setMaxLifetime(3600000L);

        String connectTimeout = "5000";

        if (connectTimeout.length() > 0) {
            config.addDataSourceProperty("oracle.net.CONNECT_TIMEOUT", connectTimeout);
        }

        String readTimeout = "5000";

        if (readTimeout.length() > 0) {
            config.addDataSourceProperty("oracle.jdbc.ReadTimeout", readTimeout);
        }

        String maximumPoolSize = "4";

        if (maximumPoolSize.length() > 0) {
            config.setMaximumPoolSize(Integer.parseInt(maximumPoolSize));
        }

        if (config.getMaximumPoolSize() > 8) {  // Will otherwise default to maximumPoolSize
            config.setMinimumIdle(8);
        }

        config.setJdbcUrl("jdbc:derby:memory:jdaxdb;create=true");
        config.setUsername("sa");
        config.setPassword("sa");

        return config;
    }

    protected void d(String s, String dsName) {
        if (Connector.dataSource(dsName) instanceof HikariDataSource hikari) {

            HikariPoolMXBean mxBean = hikari.getHikariPoolMXBean();

            logger.info(
                    "Hikari stats: total={}, active={}, idle={}, waiting={}, [ DS={}, {} ]",
                    mxBean.getTotalConnections(),
                    mxBean.getActiveConnections(),
                    mxBean.getIdleConnections(),
                    mxBean.getThreadsAwaitingConnection(),
                    dsName,
                    s);

//            int total = mxBean.getTotalConnections();
//            int active = mxBean.getActiveConnections();
//            int idle = mxBean.getIdleConnections();
//            int awaiting = mxBean.getThreadsAwaitingConnection();
//            
//            System.out.println("DS-   total = " + total);
//            System.out.println("DS-   active = " + active);
//            System.out.println("DS-   idle = " + idle);
//            System.out.println("DS-   awaiting = " + awaiting);
        }

    }
}
