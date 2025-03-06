package no.redeye.lib.jdax;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;
import javax.sql.DataSource;
import no.redeye.lib.jdax.sql.DBQueries;

/**
 */
public class TestBase {

    protected final String DATASOURCE_NAME = "jdax-ds";
    private final DBQueries dbq = new DBQueries(DATASOURCE_NAME);

    protected final int ID = 1;
    protected final int INTEGER_VALUE = 1010101010;
    protected final long BIGINT_VALUE = 1234567890123456789l;
    protected final float REAL_VALUE = 1234567890123456789.0f;
    protected final double FLOAT_VALUE = 1234567890123456789.1d;
    protected final double DOUBLE_VALUE = 1234567890123456789.2d;
    protected final BigDecimal DECIMAL_VALUE = new BigDecimal("123456789012345678.90");
    protected final BigDecimal NUMERIC_VALUE = new BigDecimal("123456789012345678.91");
//    protected final BigDecimal DECIMAL_VALUE = new BigDecimal("123456789.90");
//    protected final BigDecimal NUMERIC_VALUE = new BigDecimal("123456789.91");
    protected final LocalDate DATE_VALUE = LocalDate.now().plusDays(7);
    protected final LocalTime TIME_VALUE = LocalTime.MIN;
    protected final Instant TIMESTAMP_VALUE = Instant.now();
    protected final String CHAR_VALUE = "char";
    protected final String VARCHAR_VALUE = "string";
    protected final InputStream BLOB_VALUE = null;
    protected final Reader CLOB_VALUE = null;

    static {
        System.setProperty("derby.stream.error.file", "target/derby.log");
    }

    protected void setUpDS(Features... features) throws SQLException {
        initDS(features);
    }

    protected void setUpTestTables() throws SQLException {
        dbq.createTestTables();
    }

    protected void setUpTypesTable(String tableName) throws SQLException {
        dbq.createMultiTypesTable(tableName);
    }

    protected void tearDownDS() {
        Connector.close(DATASOURCE_NAME);
        Connector.remove(DATASOURCE_NAME);
    }

    protected void createRecordWithNonNullFields() {
        Connector.close(DATASOURCE_NAME);
        Connector.remove(DATASOURCE_NAME);
    }

    private String blob(InputStream is) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        try (is) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // You can adjust the buffer size as needed
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // Convert the byte array to a string using UTF-8 encoding
            sb.append(new String(outputStream.toByteArray(), "UTF-8"));
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }

        sb.append("}");
        return sb.toString();
    }

    private String blob(byte[] bs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (null != bs) {
            for (byte b : bs) {
                sb.append(" ").append(b);
            }
        }
        sb.append("}");
        return sb.toString();
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

    private synchronized void initDS(Features... features) throws SQLException {
        DataSource dataSource = dataSource();

        Function<String, DataSource> dsCreator = new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        };

        Connector.prepare(DATASOURCE_NAME, dsCreator, features);
    }

    private synchronized DataSource dataSource() {
        return new HikariDataSource(config());
    }

    private HikariConfig config() {
        HikariConfig config = new HikariConfig();
//        config.setDriverClassName("oracle.jdbc.OracleDriver");
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

        String maximumPoolSize = "1";

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
}
