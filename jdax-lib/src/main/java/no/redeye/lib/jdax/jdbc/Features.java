package no.redeye.lib.jdax.jdbc;

/**
 * Flags used to declare which Connector features to use during execution.
 * <p>
 * {@link #USE_GENERATED_KEYS_FLAG}
 * {@link #AUTO_COMMIT_ENABLED}
 * {@link #AUTO_COMMIT_DISABLED}
 * {@link #READ_ONLY_MODE}
 */
public enum Features {
    /**
     * Use Statement.RETURN_GENERATED_KEYS to retrieve ID of newly inserted rows.
     * If not set, the DAO will use a named field instead of auto-generated column.
     */
    USE_GENERATED_KEYS_FLAG, 
    /**
     * set connection to auto-commit mode
     */
    AUTO_COMMIT_ENABLED, 
    /**
     * disable connection auto-commit
     */
    AUTO_COMMIT_DISABLED,
    /**
     * put connection in read-only mode
     */
    READ_ONLY_MODE;

}
