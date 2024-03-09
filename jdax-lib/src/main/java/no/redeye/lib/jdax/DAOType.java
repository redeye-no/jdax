package no.redeye.lib.jdax;

//import java.lang.reflect.Field;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.redeye.lib.jdax.jdbc.Connector;
import no.redeye.lib.jdax.jdbc.Features;
import no.redeye.lib.jdax.jdbc.SQLTypeConverter;
import no.redeye.lib.jdax.types.QueryInputs;
import no.redeye.lib.jdax.types.QueryResults;
import no.redeye.lib.jdax.types.ResultRows;
import no.redeye.lib.jdax.types.VO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DAOType is a data access class that parses and prepares SQL queries for execution.
 * This class provides convenience methods for all CRUD operations.
 */
public class DAOType {

    protected static Logger logger = LogManager.getLogger("apiLogger");

    private final Pattern SQL_STATEMENT_PARAM_MARKERS = Pattern.compile(",?[ ]*'?#[a-zA-Z0-9_\\-.: ]*'?|,?[ ]*\\?{2}|,?[ ]*\\?");

    private final String DS_NAME;

    /**
     * 
     * @param datasourceName The datasourceName associated datasource declared
     * in the Connector
     */
    public DAOType(String datasourceName) {
        DS_NAME = datasourceName;
    }

    /**
     * Execute select query with no bind values.
     * <p>
     * <
     * pre>select from something</pre>
     *
     * @param sql
     * @param ins
     *
     * @return
     *
     * @throws SQLException
     */
    public ResultRows select(String sql, Object[]... ins) throws SQLException {
        return select((Object[]) null, sql, ins);
    }

    /**
     * Execute select query with provided bind values.
     * <p>
     * <
     * pre>select from something where thing = :values:</pre>
     *
     * @param vo
     * @param sql
     * @param ins
     *
     * @return
     *
     * @throws SQLException
     */
    public ResultRows select(VO vo, String sql, Object[]... ins) throws SQLException {
        Object[] values = fields(vo);
        return select(values, sql, ins);
    }

    /**
     * Execute select query with provided values and parameters.
     * <p>
     * <
     * pre>select from something
     * where thing = :values:</pre>
     * <p>
     * <p>
     * <
     * pre>select from something
     * where thing = :wheres:
     * and another in (:ins:)
     * </pre>
     * Example:
     * <p>
     * <
     * pre>select a ,b from thing
     * where (c = ?
     * and d in (??)
     * and e in (??))
     * or f = ?</pre>
     * <p>
     * This query would then be implemented as:
     * <p>
     * <
     * pre>select(new Object[] {c, f},
     * new Object[][] {
     * {"d1", "d2", "d3"},
     * {e1, e2, e3}},
     * query);</pre>
     *
     * @param values
     * @param ins
     * @param sql
     *
     * @return
     *
     * @throws SQLException
     */
    public ResultRows select(Object[] values, String sql, Object[]... ins) throws SQLException {
        QueryInputs qi = buildQueryInputs(values, ins, sql);
        return executeQuery(qi);
    }

    /**
     * Execute insert statement with values from provided VO.This convenience
     * method returns a single identity value for the inserted row.
     *
     * @param clazz
     * @param sql
     * @param returnFields field name whose value is returned as identity. If
     * blank (""), then the default identity is returned.
     *
     * @return identity value for the new record
     *
     * @throws SQLException
     */
    @SafeVarargs
    public final long insertOne(VO clazz, String sql, String... returnFields) throws SQLException {
        Object[] values = fields(clazz);
        return insertOne(values, sql, returnFields);
    }

    /**
     * Execute insert statement with provided bind values.This convenience
     * method returns a single identity value for the inserted row. -1, if no
     * rows were inserted.
     *
     * @param values
     * @param sql
     * @param returnFields field name whose value is returned as identity
     *
     * @return identities of inserted rows, or number of inserted rows if fields
     * is null
     *
     * @throws SQLException
     */
    @SafeVarargs
    public final long insertOne(Object[] values, String sql, String... returnFields) throws SQLException {
        QueryInputs qi = buildQueryInputs(values, null, sql);
        QueryResults qr = executeInsert(qi, returnFields);

        if (returnFields.length == 0) {
            return qr.count();
        }
        if ((null != qr.ids()) && (!qr.ids().isEmpty())) {
            return qr.ids().get(0);
        }
        return -1;
    }

    /**
     * Execute insert statement with provided bind values.
     *
     * @param clazz
     * @param returnFields
     * @param sql
     *
     * @return number of inserted rows
     *
     * @throws SQLException
     */
    @SafeVarargs
    public final List<Long> insert(VO clazz, String sql, String... returnFields) throws SQLException {
        Object[] values = fields(clazz);
        QueryInputs qi = buildQueryInputs(values, null, sql);
        return insert(values, sql, returnFields);
    }

    /**
     * Execute insert statement with provided bind values.
     *
     * @param values
     * @param sql
     * @param returnFields
     *
     * @return number of inserted rows
     *
     * @throws SQLException
     */
    @SafeVarargs
    public final List<Long> insert(Object[] values, String sql, String... returnFields) throws SQLException {
        QueryInputs qi = buildQueryInputs(values, null, sql);
        return executeInsert(qi, returnFields).ids();
    }

    /**
     * Update table using VO
     * <p>
     * <
     * pre>update/delete something where another in (:ins:)</pre>
     *
     * @param clazz
     * @param sql
     * @param ins
     *
     * @return updated rows count
     *
     * @throws SQLException
     */
    public int update(VO clazz, String sql, Object[]... ins) throws SQLException {
        return update(clazz, null, sql, ins);
    }

    /**
     * Update table using VO
     * <p>
     * <
     * pre>update something set thing1 = :class.thing1, thing2 = :class.thing2
     * where another = :wheres:</pre>
     *
     * @param clazz
     * @param wheres
     * @param sql
     * @param ins
     *
     * @return updated rows count
     *
     * @throws SQLException
     */
    public int update(VO clazz, Object[] wheres, String sql, Object[]... ins) throws SQLException {
        Object[] values = fields(clazz);
        return update(values, wheres, sql, ins);
    }

    /**
     * Update table.
     * <p>
     * <
     * pre>update/delete something where another in (:ins:)</pre>
     *
     * @param values - new values
     * @param ins
     * @param sql
     *
     * @return updated rows count
     *
     * @throws SQLException
     */
    public int update(Object[] values, String sql, Object[]... ins) throws SQLException {
        QueryInputs qi = buildQueryInputs(values, null, ins, sql);
        return executeUpdate(qi).count();
    }

    /**
     * Update table.
     * <p>
     * <
     * pre>update something
     * set thing1 = :values.1, thing2 = :values.2
     * where another = :wheres:
     * and another in (:ins:)</pre>
     *
     * @param values - new values
     * @param wheres - where clause values
     * @param ins
     * @param sql
     *
     * @return updated rows count
     *
     * @throws SQLException
     */
    public int update(Object[] values, Object[] wheres, String sql, Object[]... ins) throws SQLException {
        QueryInputs qi = buildQueryInputs(values, wheres, ins, sql);
        return executeUpdate(qi).count();
    }

    private QueryInputs buildQueryInputs(Object[] values, Object[] wheres, Object[][] ins, String sql) throws SQLException {
        if (null != values) {
            if (null != wheres) {
                // Merge all values
                Object[] allValues = new Object[values.length + wheres.length];
                System.arraycopy(values, 0, allValues, 0, values.length);
                System.arraycopy(wheres, 0, allValues, values.length, wheres.length);
                return buildQueryInputs(allValues, ins, sql);
            }
            return buildQueryInputs(values, ins, sql);
        }
        return buildQueryInputs(wheres, ins, sql);
    }

    private QueryInputs buildQueryInputs(Object[] wheres, Object[][] ins, String sql) throws SQLException {
        if (null == sql) {
            throw new SQLException("Query statement is null");
        }

        StringBuilder bigQuery = new StringBuilder();
        List<Object> allValues = new ArrayList();

        // Find all ? and ?? in query, then rebuild the final wheres array.
        // ? params are copied from the values input array,
        // ?? gets expanded to multiple comma-separated ?
        Matcher matcher = SQL_STATEMENT_PARAM_MARKERS.matcher(sql);

        int queryBuilderIndex = 0;
        int valuesSrcIndex = 0;
        int insArrayIndex = 0;
        boolean isFirstParam = true;

        while (matcher.find()) {
            bigQuery.append(sql.substring(queryBuilderIndex, matcher.start()));

            String match = sql.substring(matcher.start(), matcher.end());

            // Query reconstruction flags
            boolean prependComma = match.startsWith(",");
            boolean isReplaceClause = match.replaceAll(",", "").trim().length() > 2;
            boolean isInClause = match.replaceAll(",", "").trim().equals("??");
            boolean isSkipClause = match.replaceAll(",", "").trim().equals("#");

            if (isInClause || isSkipClause) {
                prependComma = false;
            }

            String clause = match.replaceAll(",", "").trim().substring((isReplaceClause ? 1 : 0));

            if (isInClause) {
                // Convert IN clause markers to statement params.
                // In: (??) Out: (?, ?, ?)
                allValues.addAll(Arrays.asList(ins[insArrayIndex]));
                if (prependComma && !isFirstParam) {
                    bigQuery.append(", ");
                }
                bigQuery.append(expandInsParams(ins[insArrayIndex].length));
                insArrayIndex++;
                isFirstParam = false;
            } else if (isReplaceClause) {
                // In-line replacement of statement markers.
                // In: (#sequence.nextval, ?) Out: (sequence.nextval, ?)
                if (prependComma && !isFirstParam) {
                    bigQuery.append(", ");
                }
                bigQuery.append(clause);
                valuesSrcIndex++;
                isFirstParam = false;
            } else if (isSkipClause) {
                // Completely skip parameter
                // In: (#, ?) Out: (?)
                valuesSrcIndex++;
            } else {
                if ((null == wheres) || (wheres.length <= valuesSrcIndex)) {
                    throw new SQLException("Insufficient number of values provided for query params");
                }
                // Copy params with no modifications.
                allValues.add(wheres[valuesSrcIndex]);
                if (prependComma && !isFirstParam) {
                    bigQuery.append(", ");
                }
                bigQuery.append(clause);
                valuesSrcIndex++;
                isFirstParam = false;
            }
            queryBuilderIndex = matcher.end();
        }

        if (queryBuilderIndex < sql.length()) {
            // Now copy the rest of the query
            bigQuery.append(sql.substring(queryBuilderIndex));
        }

        Object[] values = allValues.toArray(new Object[0]);
        return new QueryInputs(values, bigQuery.toString());
    }

    private ResultRows executeQuery(QueryInputs qi) throws SQLException {
        logger.debug("SQL: {}", qi.sql());

        PreparedStatement ps = Connector.connection(DS_NAME).prepareStatement(qi.sql());
        bind(ps, qi.values());
        return new ResultRows(ps.executeQuery(), ps);
    }

    private QueryResults executeInsert(QueryInputs qi, String[] fields) throws SQLException {
        logger.debug("SQL: {}", qi.sql());
        boolean isReturningGeneratedKeys = true;
        PreparedStatement ps;
        if (Connector.enabled(DS_NAME, Features.USE_GENERATED_KEYS_FLAG)) {
            ps = Connector.connection(DS_NAME).prepareStatement(qi.sql(), Statement.RETURN_GENERATED_KEYS);
        } else if (fields.length > 0) {
            ps = Connector.connection(DS_NAME).prepareStatement(qi.sql(), fields);
        } else {
            ps = Connector.connection(DS_NAME).prepareStatement(qi.sql());
            isReturningGeneratedKeys = false;
        }
        return query(ps, qi, !isReturningGeneratedKeys);
    }

    private QueryResults executeUpdate(QueryInputs qi) throws SQLException {
        logger.debug("SQL: {}", qi.sql());
        boolean returnCount = true;

        PreparedStatement ps = Connector.connection(DS_NAME).prepareStatement(qi.sql());

        return query(ps, qi, returnCount);
    }

    private QueryResults query(PreparedStatement ps, QueryInputs qi, boolean returnCount) throws SQLException {
        try (ps) {
            bind(ps, qi.values());

            int updateCount = ps.executeUpdate();
            logger.debug("Update count: {}", updateCount);
            if (returnCount) {
                return new QueryResults(updateCount);
            } else {
                // Retrieve the table identity number
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    List<Long> identities = new ArrayList();
                    if (null != keys) {
                        while (keys.next()) {
                            identities.add(keys.getLong(1));
                        }
                    }
                    return new QueryResults(identities, 0); // 0, to satisfy disambiguate QueryResults(List<Long>)
                }
            }
        }
    }

    private String expandInsParams(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(",?");
        }
        return sb.substring(1);
    }

    /**
     * Bind query values to prepared statement.
     *
     * @param ps
     * @param id
     * @param values
     *
     * @throws SQLException
     */
    protected void bind(PreparedStatement ps, long id, Object[] values) throws SQLException {
        Object[] bindValues = new Object[values.length];
        bindValues[0] = id;
        System.arraycopy(values, 1, bindValues, 1, bindValues.length - 1);
        bind(ps, bindValues);
    }

    /**
     * Dynamic binding of values to corresponding SQL types.
     *
     * @param ps
     * @param values
     *
     * @throws SQLException
     */
    private void bind(PreparedStatement ps, Object[] values) throws SQLException {
        for (int i = 1; i <= values.length; i++) {
            Object value = values[i - 1];
            SQLTypeConverter.setValueForType(ps, value, i);
        }
    }

    /**
     * Extract all field values from the clazz record. The skipFields indices
     * allows this method to drop ID fields from bind variables.
     *
     * @param clazz
     * @param skipFields
     *
     * @return
     *
     * @throws SQLException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private Object[] fields(VO clazz) throws SQLException {
        Class classType = clazz.getClass();
        RecordComponent[] rcs = classType.getRecordComponents();
        if (null == rcs) {
            throw new SQLException("Invalid VO, type must be a Java record");
        }

        try {
            Object[] fields = new Object[rcs.length];

            int fieldIndex = 0;
            int skipIndex = 0;

            for (int i = 0; i < rcs.length; i++) {
                RecordComponent rc = rcs[i];
                Field field = classType.getDeclaredField(rc.getAccessor().getName());
                Method method = classType.getDeclaredMethod(field.getName(), (Class<?>[]) null);

                fields[i - skipIndex] = method.invoke(clazz, (Object[]) null);
                fieldIndex++;
            }
            return fields;
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new SQLException(ex);
        }
    }
}
