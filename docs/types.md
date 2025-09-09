## Data types

This library uses the following conversion table when working with the different SQL data types.
The alt. type column shows alternate types that a field can be retrieved as.

| SQL Type                            | Default Java Type | Other Supported Java Types                                    | Default for Null (fallback value)   |
|-------------------------------------|-----------|-----------------------------------------------------------------------|-------------------------------------|
| `VARCHAR`, `LONGNVARCHAR`           | `String`  |                                                                       | `""` (empty string)                 |
| `CHAR`                              | `Character[]` | `String`                                                          | `new Character[0]` (empty char array) |
| `DECIMAL`, `NUMERIC`                | `BigDecimal` | `BigInteger`, `Integer`, `Long`, `Double`, `Float`, `Short`, `String` | `BigDecimal.ZERO`                |
| `BIT`, `BOOLEAN`                    | `Boolean` | `String` (`"true"/"false"`)                                           | `Boolean.FALSE`                     |
| `SMALLINT`                          | `Short`   | `Integer`, `Long`, `Double`, `Float`, `Byte`, `String`                | `Short.valueOf(0)`                  |
| `INTEGER`                           | `Integer` | `Long`, `Double`, `Float`, `Short`, `Byte`, `String`                  | `Integer.valueOf(0)`                |
| `TINYINT`                           | `Byte`    |                                                                       | `Byte.valueOf(0)`                   |
| `BIGINT`                            | `Long`    | `Integer`, `Double`, `Float`, `Short`, `String`                       | `Long.valueOf(0)`                   |
| `DOUBLE`, `FLOAT`                   | `Double`  | `Integer`, `Long`, `Float`, `Short`, `String`                         | `Double.valueOf(0.0)`               |
| `REAL`                              | `Float`   | `Integer`, `Long`, `Double`, `Short`, `String`                        | `Float.valueOf(0.0)`                |
| `BINARY`, `VARBINARY`, `LONGVARBINARY` | `byte[]`  | `BigInteger`                                                       | `new byte[0]` (empty byte array)    |
| `DATE`                              | `LocalDate` |                                                                     | `LocalDate.EPOCH`                   |
| `TIME`, `TIME_WITH_TIMEZONE`        | `LocalTime` |                                                                     | `LocalTime.MIDNIGHT`                |
| `TIMESTAMP`, `TIMESTAMP_WITH_TIMEZONE` | `Instant` | `Long` (epoch millis),  (`Instant.EPOCH`)                          | `Instant.EPOCH`                     |
| `BLOB`                              | `InputStream` | `Byte[]` (read from stream)                                       | `new EmptyStream()`                 |
| `CLOB`                              | `Reader`  |                                                                       | `new StringReader("")`              |
| `Other` (`default`)                 | `Object`  | Depends on JDBC driver; registry not extended                         | No explicit default (depends on type) |

JDAX auto-scaling feature provides for simple conversion of numeric fields. For instance,
a BigDecimal field can be retrieved as an int:

    while (rows.next()) {
        BigDecimal bigPopulation = rows.getInt("big_decimal_field");
        int localPopulation = rows.getInt("big_decimal_field");
    }

The "Default for NULL" column lists the values returned for null fields when the `Features.NULL_RESULTS_DISABLED` flag is set.
This allows application to run safely without having to implement null checks for returned fields.

[Main documentation](../README.md)
