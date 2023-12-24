package no.redeye.lib.jdax.types;

/**
 * A transfer object for SQL query inputs
 */
public record QueryInputs(Object[] values, String sql) {

}
