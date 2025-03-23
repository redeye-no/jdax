## Data types

This library uses the following conversion table when working with the different SQL data types:

| SQL type | Java type | Default for NULL |
|---|---|---|
| VARCHAR   | java.lang.String | "" |
| CHAR      | java.lang.String | "" |
| LONGNVARCHAR | java.lang.String | "" |
| DECIMAL   | BigDecimal | BigDecimal.ZERO |
| NUMERIC   | BigDecimal | BigDecimal.ZERO |
| BIT       | boolean | false |
| BOOLEAN       | boolean | false |
| SMALLINT  | short | 0 |
| INTEGER   | int | 0 |
| TINYINT   | byte | 0 |
| BIGINT    | long | 0l |
| DOUBLE    | double | 0.0d |
| FLOAT     | double | 0.0d |
| REAL      | float | 0.0f |
| BINARY    | byte[] | byte[0] |
| VARBINARY | byte[] | byte[0] |
| LONGVARBINARY | byte[] | byte[0] |
| NULL      | null | null |
| DATE      | java.time.LocalDate | LocalDate.EPOCH |
| TIME      | java.time.LocalTime | LocalTime.MIDNIGHT |
| TIMESTAMP | java.time.Instant | Instant.EPOCH |
| BLOB      | java.io.InputStream | empty Stream |
| CLOB      | java.io.Reader | empty Reader |
| (other)   | Object | null |

JDAX auto-scaling feature provides for simple conversion of numeric fields. For instance,
a BigDecimal field can be retrieved as an int:

    while (rows.next()) {
        BigDecimal bigPopulation = rows.getInt("big_decimal_field");
        int localPopulation = rows.getInt("big_decimal_field");
    }

The "Default for NULL" column lists the values returned for null fields when the Features.NULL_RESULTS_DISABLED flag is set.
This allows application to run safely without having to implement null checks for returned fields.

[Main documentation](../README.md)
