package no.redeye.lib.jdax.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Connector {

    private static final Logger logger = LogManager.getLogger("apiLogger");

    private record DataSourceEntry(DataSource dataSource, int features) {

    }

    private static final Map<String, DataSourceEntry> datasources = new ConcurrentHashMap<>();

    private static final ThreadLocal<AtomicInteger> activityCount = new ThreadLocal<>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };

    private static final ThreadLocal<Map<String, Connection>> threadLocalMap = new ThreadLocal<>() {
        @Override
        protected Map<String, Connection> initialValue() {
            return new ConcurrentHashMap<>();
        }
    };

    /**
     * Prepare the ThreadLocal for binding a new DataSource. This method accepts
     * a Function that returns a DataSource when applied.
     *
     * @param key
     * @param function
     * @param flags
     *
     * @throws SQLException
     */
    public static void prepare(String key, Function<String, DataSource> function, Features... flags) throws SQLException {
        if (null == function) {
            throw new SQLException("The provided connection cannot be null, (key=" + key + ")");
        }

        DataSource ds = function.apply(key);
        prepare(key, ds, flags);
    }

    /**
     * Add DataSource to the registry. An SQLException is thrown if the key
     * already exists.
     *
     * @param key
     * @param dataSource
     * @param flags
     *
     * @throws SQLException
     */
    public static void prepare(String key, DataSource dataSource, Features... flags) throws SQLException {
        if (null == dataSource) {
            throw new SQLException("Cannot register a null datasource, (key=" + key + ")");
        }

        int features = 0;
        if (null != flags) {
            for (Features flag : flags) {
                features |= (1 << flag.ordinal());
            }
        }

        if (datasources.containsKey(key)) {
            return;
        }

        datasources.put(key, new DataSourceEntry(dataSource, features));
    }

    /**
     * Remove DataSource from the registry.
     *
     * @param key
     */
    public static void remove(String key) {
        datasources.remove(key);
        logger.trace("Remove datasource, (key=" + key + ")");
    }

    /**
     * Return a flag indicating whether the connection supports the feature.
     *
     * @param key
     * @param feature
     *
     * @return
     */
    public static boolean enabled(String key, Features feature) {
        if (datasources.containsKey(key)) {
            return ((datasources.get(key).features() & (1 << feature.ordinal())) != 0);
        }
        return false;
    }

    private static void configure(String key, Connection connection) throws SQLException {
        connection.setAutoCommit(enabled(key, Features.AUTO_COMMIT_ENABLED) || !enabled(key, Features.AUTO_COMMIT_DISABLED));
        connection.setReadOnly(enabled(key, Features.READ_ONLY_MODE));
    }

    /**
     * Check if the connection associated with the provided key is ready for
     * use. If this method returns false, then either close() has already been
     * called, or prepare() failed.
     *
     * @param key
     *
     * @return
     */
    public static boolean ready(String key) {
        return datasources.containsKey(key);
    }

    private static boolean active() {
        return activityCount.get().get() > 0;
    }

    /**
     * Get the connection bound to the current thread.
     *
     * @param key
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    public static Connection connection(String key) throws SQLException {
        return connection(key, true);
    }

    private static Connection connection(String key, boolean createNew) throws SQLException {
        if (!threadLocalMap.get().containsKey(key)) {
            if (!createNew) {
                throw new SQLException("Illegal operation on null connection");
            }
            if (!datasources.containsKey(key)) {
                throw new SQLException("Illegal operation on null connection");
            }
            if (null == datasources.get(key).dataSource()) {
                throw new SQLException("null: datasource (" + key + ") does not exist");
            }

            Connection connection = datasources.get(key).dataSource().getConnection();
            configure(key, connection);
            threadLocalMap.get().putIfAbsent(key, connection);
            activityCount.get().incrementAndGet();
        }
        return threadLocalMap.get().get(key);
    }

    /**
     * Commit the connection.
     *
     * @param key
     *
     * @throws SQLException
     */
    public static void commit(String key) throws SQLException {
        if (!active()) {
            return;
        }

        if (!ready(key)) {
            logger.error("Cannot commit a null connection");
            return;
        }

        try {
            logger.trace("Connection commit");
            connection(key, false).commit();
        } catch (SQLException se) {
            logger.error("Error committing connection: {}", se);
            throw se;
        }
    }

    /**
     * Connection rollback.
     *
     * @param key
     *
     * @throws SQLException
     */
    public static void rollback(String key) throws SQLException {
        if (!active()) {
            return;
        }

        if (!ready(key)) {
            logger.error("Cannot rollback a null database connection");
            return;
        }

        try {
            logger.trace("Connection rollback");
            connection(key, false).rollback();
        } catch (SQLException se) {
            logger.error("Error rolling back connection: {}", se);
            throw se;
        }
    }

    /**
     * Close connection and remove it from thie current thread.
     *
     * @param key
     */
    public static void close(String key) {
        if (!active()) {
            return;
        }
        // Remove it from the thread
        threadLocalMap.get().computeIfPresent(key, (k, v) -> {
            // Close it
            close(k, v);
            return null;
        });
    }

    private static void close(String key, Connection connection) {
        if (null == connection) {
            logger.error("Cannot close a null database connection (sourceRef=" + key + ")");
            return; // Silent exit
        }

        try (connection) {
            activityCount.get().decrementAndGet();
            logger.trace("Connection close");
        } catch (SQLException e) {
            logger.error("Error closing connection (sourceRef={}): {}", key, e);
        }
    }
}
