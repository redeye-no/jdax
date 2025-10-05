package no.redeye.lib.jdax.types;

/**
 *
 */
public sealed interface QueryResults permits InsertResults, UpdateResults {
    int count();
}
