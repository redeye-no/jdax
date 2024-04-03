# jdax

A data access layer on top of JDBC for implementing CRUD operation.

## Specifications and requirements

This library implements an in-memory cache mechanism for managing the lifecycle of stored data. This library
utilises Java generics to provide a cache that supports a wide variety of data types.

## Getting the library

    <dependency>
        <groupId>no.redeye</groupId>
        <artifactId>jdax-lib</artifactId>
    </dependency>

## Programming against the library API

The following objects make up the basic dal framework:

|Class	|Description|
|-------|-----------|
| `Connector`	| Provides a mechanism for managing JDBC connections. |
| `DAOType`	| Access class providing read, write and delete operations. |
| `Features`	| Flags for configuring the behaviour of the connections. |
| `VO`          | A type definition for data access and value objects. |
| `ResultRows`  | A return type with DB query results. |

## Configuring the API

The first call to the API must configure the connection parameters. This is done through the `Connector` class.

| Method | Description |
|---|---|
| `Connector.prepare()` | Set up a connectin to a datasource |

The prepare method has 2 variants, 1 of which must be called during pool configuration (usually startup):

```java
Connector.prepare(dsName, DataSourceFunction, Features);
Connector.prepare(dsName, DataSource, Features);
```

One takes a function that returns a DataSource, the other takes a configured DataSource.

The optional `Features` are used for setting various connection flags.

| Feature | Flag |
|---|---|
| `USE_GENERATED_KEYS_FLAG` | DataSource supports Statement.RETURN_GENERATED_KEYS to retrieve ID of newly inserted rows |
| `NULL_RESULTS_DISABLED` | Returns default values for NULL fields in the DB |
| `AUTO_COMMIT_ENABLED` | Set connection to auto-commit mode |
| `AUTO_COMMIT_DISABLED` | Disable connection auto-commit |
| `READ_ONLY_MODE` | Put connection in read-only mode |

### Working with the library

The `Connector` manages the lifecycle of all connections. During init, a DataSource is
stored into the object cache and made available to the application.

#### DAOType methods

| Method | Description |
|---|---|
| `DAOType.insert()` | Insert records into the DB using the SQL query provided. Input data is either a VO, or an array of values |
| `DAOType.insertOne()` | Insert a single record, and conveniently return a primitive count, or identity |
| `DAOType.select()` | Select records from the DB using the SQL query provided |
| `DAOType.update()` | Update DB records using the SQL query provided. Input data is either a VO, or an array of values |

Each of these methods take inputs for specifying the query to run, query parameters, and result hints:

| Method inputs | Description | Used in | Example |
|---|---|---|---|
| `String sql` | An SQL statement to be executed | `insert()`, `insertOne()`, `select()`, `update()` | `select id, name from sku where id = ? and status in (?)` |
| `Object[] values` | Values for use in the SQL statement | `insert()`, `insertOne()`, `select()`, `update()` | `new Object[]{ 127 }` |
| `Object[] wheres` | Values used in SQL WHERE clauses | `select()`, `update()` | `new Object[]{ 213 }` |
| `Object[] ins` | Values used in SQL IN clauses | `select()`, `update()` | `new Object[]{ "OUT-OF-STOCK" }` |
| `VO clazz` | Classes implementing VO can be used as input in place of `Object[] values` | `insert()`, `insertOne()`, `select()`, `update()` | `insert into numbers (id, name) values (??)` |


### Using DAOType

This document contains code and queries to illustrate how the library works. The table below shows what the DB looks like from this point on.

Table: NUMBERS

| id | scale | name |
|---|---|---|
| `1` | `0` | `zero` |
| `2` | `0` | `one` |
| `3` | `1` | `ten` |
| `4` | `2` | `hundred` |
| `5` | `3` | `thousand` |
| `6` | `6` | `million` |
| `7` | `9` | `billion` |


To run queries against the DB, an application will need an instance of the DAOType initialised with a known dataSource name.

    private final DAOType dao = new DAOType(dsName);

    private static final String SELECT_BY_ID = "select id, name from numbers where id = ?";

    public void print(int id){
        try (ResultRows results = dao.select(new Object[]{ id }, SELECT_BY_ID)) {
            while (results.next()) {
                print(
                    results.integerValue("id"), 
                    results.varchar("name"));
            }
        }
    }

The code creates a DAOType for a given datasource, and uses it to run a select query whose results get printed out.

### Query syntax

Each of the DAOType's insert, select, and update methods need a query to execute.
These methods allow the calling applications to use either raw values, or POJOs as inputs to queries.

The select operation in the next code block uses raw values provided in the Object[].

    private final DAOType dao = new DAOType(dsName);
    Object[] values = new Object[]{ 0, "zero" };
    ResultRows = dao.select(values, "select * from numbers where id = ? or name = ?");

The SQL statement expects 2 parameters (id and name), the items in Object[] will be used as input.
The `ResultRows` returned will contain all numbers whose `id = 0 or name = 'zero'`.

An insert into the same table could look like this (with raw values as input),

    Object[] values = new Object[]{ 8, 12, "trillion" };
    long inserted = dao.insertOne(values, "insert into numbers (id, scale, name) values (?, ?, ?)");

or (with Java object as input)

    Number trillion = new Number(8, 12, "trillion");
    long inserted = dao.insertOne(trillion, "insert into numbers (id, scale, name) values (?, ?, ?");

Number is defined as,

    public record Number(int id, int scale, String name) implements VO {}

### Advanced queries 1 (parameter expansion)

One powerful feature of this library is parameter expansion, which adjusts how the SQL statements are prepared before execution.
For instance, the following query

    select * from numbers where id in (?, ?)

is designed to expect 2 input values (one for each parameter denoted by the ?).

If the input later has 4 values, the prepared statement would fail, and requre the query to be rewritten to

    select * from numbers where id in (?, ?, ?, ?)

Parameter expansion in jdax removes the need to modify the query.

    public ResultRows searchByIDs(Object[] ids){
        return dao.select(ids, "select * from numbers where id in (??)");
    }

The query used in searchByIDs() will not need modification for any Object[] passed as an argument. The code will work with any of the following calls:

    searchByIDs(new Object[] {1});
    searchByIDs(new Object[] {3, 5, 7});
    searchByIDs(new Object[] {1, 3, 5, 7, 9, 11, 13});

because the library will automatically expand (??) to match the actual number of input values.

The SQL statement in the code is stable form, and does not need rewriting whenever the number of inputs changes.

### Advanced queries 2 (parameter replacement)

Another powerful feature of the library is parameter replacement, which allows the query composer to control how query inputs are used as SQL parameters:

    Number quadrillion = new Number(8, 15, "quadrillion");
    long inserted = dao.insertOne(quadrillion, "insert into numbers (id, scale, name) values (?, ?, ?");

Now, if the id column in the numbers table was an identity field whose value is autogenerated by the DB, the insert statement would fail.
The query could be rewritten to:

    long inserted = dao.insertOne(quadrillion, "insert into numbers (scale, name) values (?, ?)");

but that would not work, since the value object provided (quadrillion, the POJO) contains 3 values, and that won't match the query.

A better way to solve this is by harnessing parameter replacement.

    Number quadrillion = new Number(8, 15, "quadrillion");
    long inserted = dao.insertOne(quadrillion, "insert into numbers (scale, name) values (#, ?, ?)");

This will internally generate the follwoing statement:

    insert into numbers (scale, name) values (?, ?)

The hash (#) in the code, on its own, tells the parser to supress the value at the given position in the value object.

### Advanced queries 3 (tagged parameter replacement)

Tagged parameter replacement is a variant of parameter replacement and is illustrated in the following case:

    Number quadrillion = new Number(8, 15, "quadrillion");
    long inserted = dao.insertOne(quadrillion, "insert into numbers (id, scale, name) values (#SEQUENCE_ID.nextval, ?, ?)");

Translates to:

    insert into numbers (id, scale, name) values (SEQUENCE_ID.nextval, ?, ?)

Notice how the replacement marker (#) now has a tag right after it. This time, the parser will not simply supress the query parameter,
but will instead use the literal tag as part of the executable query.

Tags can be any clauses, constructs, or instructions that the underlying DB implementation supports, such as sequence numbers or IDs, DB functions, calls and procedures, etc.

### Advanced queries 4 (putting it all together)

The advanced features provided by DAOType make it possible to write more complex queries like

    Object[] values = new Object[]{ 101, 18, "quintillion", 9};
    Object[][] ins = new Object[][]{
            {1, 3, 5, 7, 9, 11, 13},
            {"sextillion", "septillion", "octillion"}};
    ResultRows selected = dao.select(values, ins, "
            select * from numbers 
            where 
                (id = ? or scale < ?)
            or 
                (id in (??) and (name not ?))
            or
                (scale > ? and name in (??))");

Jdax will automatically expand the in-clauses (??) with the correct number of SQL parameters:

            select * from numbers 
            where 
                (id = ? or scale < ?)
            or 
                (id in (?, ?, ?, ?, ?, ?, ?, ?, ?) and (name not ?))
            or
                (scale > ? and name in (?, ?, ?))

Let's see how each of the inputs are handled:

#### Single ? parameters

The Object[] values array

    Object[] values = new Object[]{ 101, 18, "quintillion", 9}

will be used to populate all simple parameters in the query:

            select * from numbers 
            where 
                (id = 101 or scale < 18)
            or 
                (id in (??) and (name not 'quintillion'))
            or
                (scale > 9 and name in (??))

#### Multi ? parameters

The Object[][] ins multidimensional array

    Object[][] ins = new Object[][]{
            {1, 3, 5, 7, 9, 11, 13},
            {"sextillion", "septillion", "octillion"}};

will be used to populate all simple parameters in the query:

            select * from numbers 
            where 
                (id = ? or scale < ?)
            or 
                (id in (1, 3, 5, 7, 9, 11, 13?) and (name not ?))
            or
                (scale > ? and name in ('sextillion', 'septillion', 'octillion'))

Each (??) group will have an array assigned to it so ins[0] will be use to populate id in (??), ins[1] will populate name in (??).

The SQL statement in the code is stable form, and does not need rewriting whenever the number of inputs changes.

### Retrieving query results

#### Insert results

Basic insert statement

    List<Long> inserted = dao.insertOne(values, "insert into numbers (scale, name) values (?, ?)", fieldName);

returns a List of the numeric identity of the newly inserted record. The `fieldName` argument specifies what field values to return.
If fieldName is not provided, then an empty List is returned.

Some databases do not support the use of `fieldName` to specify the identity field. Setting the `Features.USE_GENERATED_KEYS_FLAG`
can be used for those implementations, and the default identity will be returned instead.

#### Update results

Basic update statement

    int updated = dao.update(values, "update numbers set scale = ?, name = ? where name = ?)");

returns the number of updated records. values contains all the values for query input parameters.

The second form is

    int updated = dao.update(values, wheres, "update numbers set scale = ?, name = ? where name = ?)", ins);

which allows for more detailed statements that consist of input values, where clauses and optional in clauses.
This also returns the number of updated records.

#### Select results

Select statements always return `ResultRows`, which allow the calling application to access the retrieved data:

    ResultRows rows = dao.select(new Object[]{}, "select id, scale, name from numbers");

`ResultRows` allows the application to iterate through the query results just like the JDBC ResultSet:

    while (rows.next()) {
        print(
            rows.integerValue("id"),
            rows.varchar("name")
        );
    }

Alternatively, `ResultRows` can return row data as a Java object using the `ResultRows.get()` method:

    while (rows.next()) {
        Number number = rows.get(Number.class);
        print( number );
    }

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

The "Default for NULL" column lists the values returned for null fields when the Features.NULL_RESULTS_DISABLED flag is set.
This allows application to run safely without having to implement null checks for rturned fields.
