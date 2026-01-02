package no.redeye.lib.jdax;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectorContext implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger("apiLogger");

    private final String id;

    // dsName -> Connection
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    ConnectorContext(String contextId) {
        this.id = contextId;
    }

    /**
     * Context ID.
     * 
     * @return 
     */
    public String id() {
        return id;
    }

    /**
     * Context connection.
     * 
     * @param datasourceName
     * @return
     * @throws SQLException 
     */
    public Connection connection(String datasourceName) throws SQLException {
        logger.debug("ConnectorContext open(id={}, ds={}, count={})", id, datasourceName, connections.size());
        
        return connections.computeIfAbsent(id, key -> {
            logger.info(
                    "ConnectorContext Open new connection [{}]",
                    id
            );
            try {
                return Connector.connection(datasourceName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Context connection.
     * 
     * @param contextId
     * @return 
     */
//    Connection connection(String contextId) {
//        logger.debug("ConnectorContext connection(id={}, count={})", contextId, connections.size());
//        logger.trace("ConnectorContext Connections: {}", connections);
//        return connections.get(contextId);
//    }

    /**
     * Commit context (and associated connection).
     * 
     * @throws SQLException 
     */
    public void commit() throws SQLException {
        commit(true);
    }

    /**
     * Rollback context (and associated connection).
     * @throws SQLException 
     */
    public void rollback() throws SQLException {
        commit(false);
    }

    private void commit(boolean commit) throws SQLException {
        logger.debug("ConnectorContext {}(id={}, count={})", (commit ? "commit" : "rollback"), id, connections.size());
        SQLException first = null;
        for (String k : connections.keySet()) {
            try {
                Connection c = connections.get(k);
                if ((null != c) && !c.isClosed()) {
                    if (commit) {
                        c.commit();
                    } else {
                        c.rollback();
                    }
                }
            } catch (SQLException e) {
                logger.warn("Connections {} fail: {}", commit ? "commit" : "rollback", e);
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

    @Override
    public void close() throws SQLException {
        logger.debug("ConnectorContext close(id={}, count={})", id, connections.size());

        SQLException first = null;
        for (String k : connections.keySet()) {
            try (Connection c = connections.get(k)) {
        logger.debug("ConnectorContext close connection(id={})", id);
            } catch (SQLException e) {
                logger.warn("Connections context close fail: {}", e);
                if (first == null) {
                    first = e;
                } else {
                    first.addSuppressed(e);
                }
            }
        }
        connections.clear();
        if (first != null) {
            throw first;
        }
    }
}
