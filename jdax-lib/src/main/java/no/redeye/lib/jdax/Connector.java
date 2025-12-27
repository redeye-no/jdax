package no.redeye.lib.jdax;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Connector {

    private static final Logger logger = LogManager.getLogger("apiLogger");

    private static final Map<String, DataSourceEntry> DATASOURCES = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<String, ConnectorContext>> CONTEXTS = ThreadLocal.withInitial(HashMap::new);

    private record DataSourceEntry(DataSource dataSource, int features) {

    }

    private Connector() {
    }

    /**
     * Bind a DataSource to the current connector registry. This method accepts
     * a Function that returns a DataSource when applied.
     *
     * @param dataSourceName
     * @param function
     * @param flags
     *
     * @throws SQLException
     */
    public static void register(String dataSourceName, Function<String, DataSource> function, Features... flags) throws SQLException {
        if (null == function) {
            throw new SQLException("The provided connection cannot be null, (key=" + dataSourceName + ")");
        }

        DataSource ds = function.apply(dataSourceName);
        register(dataSourceName, ds, flags);
    }

    /**
     * Add DataSource to the registry. An SQLException is thrown if the key
     * already exists.
     *
     * @param dataSourceName
     * @param dataSource
     * @param flags
     *
     * @throws SQLException
     */
    public static void register(String dataSourceName, DataSource dataSource, Features... flags) throws SQLException {
        if (null == dataSource) {
            throw new SQLException("Cannot register a null datasource, (key=" + dataSourceName + ")");
        }

        int features = 0;
        if (null != flags) {
            for (Features flag : flags) {
                features |= (1 << flag.ordinal());
            }
        }

        DATASOURCES.putIfAbsent(dataSourceName, new DataSourceEntry(dataSource, features));
    }

    /**
     * Remove DataSource from the registry.
     *
     * @param dataSourceName
     */
    public static void deregister(String dataSourceName) {
        DATASOURCES.remove(dataSourceName);
        logger.trace("Remove datasource, (key=" + dataSourceName + ")");
    }

    /**
     * Return a flag indicating whether the connection supports the feature.
     *
     * @param dataSourceName
     * @param feature
     *
     * @return
     */
    public static boolean enabled(String dataSourceName, Features feature) {
        if (DATASOURCES.containsKey(dataSourceName)) {
            return ((DATASOURCES.get(dataSourceName).features() & (1 << feature.ordinal())) != 0);
        }
        return false;
    }

    private static void configure(String dataSourceName, Connection connection) throws SQLException {
        connection.setAutoCommit(enabled(dataSourceName, Features.AUTO_COMMIT_ENABLED) || !enabled(dataSourceName, Features.AUTO_COMMIT_DISABLED));
        connection.setReadOnly(enabled(dataSourceName, Features.READ_ONLY_MODE));
    }

    /**
     * Check if the connection associated with the provided key is ready for
     * use. If this method returns false, then either close() has already been
     * called, or register() failed.
     *
     * @param dataSourceName
     *
     * @return
     */
    public static boolean ready(String dataSourceName) {
        return DATASOURCES.containsKey(dataSourceName);
    }

    /**
     * Get a new connection from the provided dataSource.
     *
     * @param dataSourceName
     *
     * @return
     *
     * @throws java.sql.SQLException
     */
    static Connection connection(String dataSourceName) throws SQLException {
        DataSourceEntry dse = DATASOURCES.get(dataSourceName);
        if (null == dse) {
            throw new SQLException("Unknown datasource (" + dataSourceName + ")");
        }

        DataSource ds = dse.dataSource;
        Connection connection = ds.getConnection();
        configure(dataSourceName, connection);
        return connection;
    }

    private static Connection connection(String contextId, String dataSourceName)
            throws SQLException {

        if (has(contextId)) {
//            return open(dataSourceName);
            return CONTEXTS.get().get(contextId).connection(dataSourceName);
        }

        // No context -> always single
        return connection(dataSourceName);
    }

    public static boolean has(String contextId) {
        return CONTEXTS.get().containsKey(contextId);
    }

    public static DataSource ds(String dataSourceName) {
        DataSourceEntry dse = DATASOURCES.get(dataSourceName);
        if (null != dse) {
            return dse.dataSource;
        }
        return null;
    }

    /**
     * Create a new connection context with the given contest ID.
     * If one already exists, return it.
     *
     * @param contextId
     *
     * @return
     */
    public static ConnectorContext context(String contextId) {
        if (null == contextId) {
            contextId = "";
        }
        Map<String, ConnectorContext> map = CONTEXTS.get();

        System.out.println("Create contest for id=" + contextId);
        System.out.println("map out=" + map);

        return map.computeIfAbsent(contextId,
                key -> new ConnectorContext(key)
        );
    }

    /**
     * Commit context, and any associated connections.
     *
     * @param contextId
     *
     * @throws SQLException
     */
    static void commit(String contextId) throws SQLException {
        commit(contextId, true);
    }

    /**
     * Rollback context, and any associated connections.
     *
     * @param contextId
     *
     * @throws SQLException
     */
    static void rollback(String contextId) throws SQLException {
        commit(contextId, false);
    }

    private static void commit(String contextId, boolean commit) throws SQLException {
        logger.debug("Context connection {}: id={}, count={}", (commit ? "commit" : "rollback"), contextId, CONTEXTS.get().size());
        SQLException first = null;
        for (ConnectorContext context : CONTEXTS.get().values()) {
            try {
                Connection connection = context.connection(contextId);
                if (null != connection) {
                    if (!connection.isClosed()) {
                        if (commit) {
                            connection.commit();
                        } else {
                            connection.rollback();
                        }
                    }
                }
            } catch (SQLException e) {
                if (first == null) {
                    first = e;
                } else {
                    first.addSuppressed(e);
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }

    /**
     * Detach the JDAX context and clean up all resources.
     * Any open connections associated with the context ID get closed.
     *
     * @param contextId
     */
    public static void detach(String contextId) {
        logger.debug("Detach context: {}", contextId);
        Map<String, ConnectorContext> map = CONTEXTS.get();

        try (ConnectorContext cc = map.get(contextId)) {
            map.remove(contextId);
        } catch (Exception e) {
        }

        // Avoid ThreadLocal leaks
        if (map.isEmpty()) {
            logger.debug("Remove context");
            CONTEXTS.remove();
        }
    }
    
    public Map<String, DataSourceEntry> dataSources(){
        return DATASOURCES;
    }
}

//    private static final ScopedValue<ConnectorContext> CURRENT = ScopedValue.newInstance(); // Java 21+
//
//    static void set(ConnectorContext ctx) {
//        ScopedValue.where(CURRENT, ctx).run(() -> {
//        });
//    }
//
//    static ConnectorContext get() {
//        return CURRENT.get();
//    }
//
//    static void clear() {
//        // nothing to do – scope ends automatically
//    }
//}
