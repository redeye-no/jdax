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
| `DAOType`	| Super class providing read, write and delete operations. |
| `Features`	| Flags for configuring the behaviour of the connections. |
| `VO`          | A type definition for data access and value objects. |
| `ResultRows`  | A return type with DB query results. |

## Configuring the API

The first call to the API must configure the connection parameters. This is done through the Connector class.

| Method | Description |
|---|---|
| `Connector.prepare()` | Set up a connectin to a datasource |

The prepare method has 2 variants, 1 of which must be called during pool configuration (usually startup):

```java
Connector.prepare(datasourceRef, DataSourceFunction, Features);
Connector.prepare(datasourceRef, DataSource, Features);
```

One takes a function that returns a DataSource, the other takes a configured DataSource.

The optional Features are used for setting various connection flags.

| Feature | Flag |
|---|---|
| `USE_GENERATED_KEYS_FLAG` | DataSource supports Statement.RETURN_GENERATED_KEYS to retrieve ID of newly inserted rows |
| `AUTO_COMMIT_ENABLED` | Set connection to auto-commit mode |
| `AUTO_COMMIT_DISABLED` | Disable connection auto-commit |
| `READ_ONLY_MODE` | Put connection in read-only mode |

### Working with the library

The Connector manages the lifecycle of all connections. During init, a DataSource is
stored into the object cache and made available to the application.

#### DAOType methods

| Method | Description |
|---|---|
| `DAOType(String)` | Constructor with the type of VO, and a datasource reference |
| `DAOType.insert()` | Insert records into the DB using the SQL query provided. Input data is either a VO, or an array of values |
| `DAOType.insertOne()` | Insert a single record, and conveniently return a primitive count, or identity |
| `DAOType.select()` | Select records from the DB using the SQL query provided |
| `DAOType.update()` | Update DB records using the SQL query provided. Input data is either a VO, or an array of values |

### Extending DAOType

Typically, your application needs to implement a DAOType for each data model the application needs.
The following illustrates how a ProductDAO can be writen:

    public class ProductDAO extends DAOType{
        public Product(String dsName){ super(dsName); }

        private static final String SELECT_BY_ID = "select * from products where id = ?";
        public ResultRows byID(int id){
            return select(new Object[]{ id }, SELECT_BY_ID);
        }
    }

Now, each time the application needs to get Products from the database:

    final ProductDAO productDAO = new ProductDAO(dsName);

    @GET
    @Path("products")
    public Product productByID(@QueryParam int productID){
        ResultRows rows = productDAO.select(productID);
        if (rows.next()){
            return new Product(
                    rows.intValue("id"), // id field
                    rows.string("name") // name field
                );
        }
    }

### Query syntax

Each of the insert, select, and update methods need a query to execute. 
This DAL allows for any query statement supported by the underlying datasource.

The DAL API lets calling applications to use either raw values, or POJOS as inputs to queries.

To select from a DB,

    Object[] values = new Object[]{ 0, "zero" };
    ResultRows = dataStore.select(values, "select * from rowtable where scale = ? and name = ?");

will return a List of Rows from the table whose scale = 0 and name = 'zero'.
An insert into the same table could look like this,

    Object[] values = new Object[]{ 2, 1, "one" };
    long inserted = dataStore.insertOne(values, "insert into rowtable (id, scale, name) values (?, ?, ?");

or

    Row row = new Row(3, 2, "two");
    long inserted = dataStore.insertOne(row, "insert into rowtable (id, scale, name) values (?, ?, ?");

Row is defined as,

    public record Row(int id, int scale, String name) implements VO {}

and the examples assume that the following table exists,

    create table rowtable (
    id number,
    scale number,
    name varchar2(80) not null)

### Advanced queries 1 (parameter expansion)

The query rewriting feature allows jdax to provide syntax for more complex cases.

    Object[] values = new Object[]{ 1, 10, "gazillion", 9};
    Object[][] ins = new Object[][]{
            {11, 23, 29, 37, 41, 43, 47, 53, 59},
            {"sixty one", "sixty seven", "seventy one"}};
    ResultRows selected = dataStore.select(values, ins, "
            select * from rowtable 
            where 
                (id = ? or scale < ?)
            or 
                (id in (??) and (name not ?))
            or
                (scale > ? and name in (??))");

Jdax will automatically expand the in-clauses (??) with the correct number of SQL parameters:

            select * from rowtable 
            where 
                (id = ? or scale < ?)
            or 
                (id in (?, ?, ?, ?, ?, ?, ?, ?, ?) and (name not ?))
            or
                (scale > ? and name in (?, ?, ?))

Now the values in the `ins[0]` array will be used as input to `id in (*, *,...)`,
and `ins[1]` will be the input to `name in (?, ?, ?)`.
The elements in values will be used as input to `id = ?`, `scale < ?`, `name not ?`, and `scale > ?`.

The SQL statement in the code is stable form, and does not need rewriting whenever the number of inputs changes.

### Advanced queries 2 (parameter replacement)

Parameter replacement allows the query composer to control how query inputs are used as SQL parameters:

    Row row = new Row(-1, 7, "seven");
    long inserted = dataStore.insertOne(row, "insert into rowtable (id, scale, name) values (?, ?, ?)");

If the id column was an identity field whose value is autogenerated, the insert statement would fail.
The query can be rewritten as:

    Row row = new Row(-1, 7, "seven");
    long inserted = dataStore.insertOne(row, "insert into rowtable (scale, name) values (?, ?)");

but that would not work, since the VO provided (row, the POJO) will contain 3 values (jdax expands 
all the arguments in the main constructor), and that won't match the query.

A better way to solve this is by parameter replacement:

    Row row = new Row(-1, 7, "seven");
    long inserted = dataStore.insertOne(row, "insert into rowtable (scale, name) values (#, ?, ?)");

which generates an SQL like:

    insert into rowtable (scale, name) values (?, ?)

The hash (#), on its own, tells the parser to ignore the value in `Row(-1, 7, "seven")`, and the
corresponding parameter in the query.

### Advanced queries 3 (tagged parameter replacement)

A second variant of parameter replacement is illustrated in the following case:

    Row row = new Row(-1, 7, "seven");
    long inserted = dataStore.insertOne(row, "insert into rowtable (id, scale, name) values (#SEQUENCE_ID.nextval, ?, ?)");

Translates to:

    insert into rowtable (id, scale, name) values (SEQUENCE_ID.nextval, ?, ?)

Notice how the replacement marker (#) now has a tag right after it. This time, the parser will not simply ignore the query parameter,
but will instead use the literal tag as part of the executable query.
Tags can be any clauses, constructs, or instructions that the DB implementation supports.

## Data types

| SQL type | Java type |
|---|---|
| VARCHAR   | java.lang.String |
| CHAR      | java.lang.String |
| LONGNVARCHAR | java.lang.String |
| DECIMAL   | BigDecimal |
| NUMERIC   | BigDecimal |
| BIT       | boolean |
| SMALLINT  | short |
| INTEGER   | int |
| TINYINT   | byte |
| BIGINT    | long |
| DOUBLE    | double |
| FLOAT     | double |
| REAL      | float |
| BINARY    | byte[] |
| VARBINARY | byte[] |
| LONGVARBINARY | byte[] |
| NULL      | null |
| DATE      | java.time.LocalDate |
| TIME      | java.time.LocalTime |
| TIMESTAMP | java.time.Instant |
| BLOB      | byte[], InputStream |
| CLOB      | String |
| (other)   | Object |
