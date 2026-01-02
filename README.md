# jdax

A data access layer on top of JDBC for implementing CRUD operation.

JDAX is a Java library that simplifies data access operations by providing an abstraction layer over JDBC.  
It streamlines CRUD operations through a consistent API for data retrieval and management.

## Features

- Simplified JDBC connection configuration
- Consistent API for CRUD operations
- Effective SQL syntax safe from injection attacks
- Advanced query augmentation
- Almost seamless integration with existing JDBC code
- Support for both basic data types (ints and Strings) to complex objects and records
- Explicit and portable connection and transaction handling

## Installation

```xml
<dependency>
    <groupId>no.redeye</groupId>
    <artifactId>jdax-lib</artifactId>
</dependency>
```

## Programming against the library API

The following objects make up the basic framework:

| Class        | Description                                               |
| ------------ | --------------------------------------------------------- |
| `Connector`  | Provides a mechanism for managing JDBC connections.       |
| `DAOType`    | Access class providing read, write and delete operations. |
| `Features`   | Flags for configuring the behaviour of the connections.   |
| `VO`         | A type definition for data access and value objects.      |
| `ResultRows` | A return type with DB query results.                      |

## Connection and transaction model

JDAX does **not** rely on container-managed transactions (`@Transactional`) or JTA.
Instead, it uses an explicit *unit-of-work* model that provides:

* Manual control over `commit()` and `rollback()`
* Support for multiple DataSources in the same unit of work
* Deterministic connection lifecycle
* Usability both inside and outside Jakarta EE containers

Connections are acquired through `Connector`, scoped to a logical unit of work, 
and managed internally by context.

For a full architectural overview, see:

* [JDAX connection architecture](docs/architecture.md)

## Usage

### Establishing a Connection

The first call to the API must configure the connection parameters. This is done through the `Connector` class.

| Method                | Description                         |
| --------------------- | ----------------------------------- |
| `Connector.register(()` | Set up a connection to a datasource |

The register( method has 2 variants, one of which must be called during pool configuration (usually startup):

```java
Connector.register((dsName, DataSourceFunction, Features);
Connector.register((dsName, DataSource, Features);
```

One takes a function that returns a `DataSource`, the other takes a configured `DataSource`.

The optional `Features` are used for setting various connection flags.

Example:

```java
DataSource dataSource = new HikariDataSource(new HikariConfig());
Connector.register(("ds-users", dataSource, Features.AUTO_COMMIT_ENABLED);
```

[More details on establishing connections](docs/connections.md)

### Running queries

The `DAOType` manages query executions and transforms result sets.
Applications can either call `DAOType` directly or inherit from it when implementing custom data access objects (DAOs).

```java
DAOType dt = new DAOType("ds-users");
ResultRows users = dt.select("select * from users");
```

#### Selecting data

Query result sets are always returned in a `ResultRows` wrapper object that provides a JDAX API for retrieving data.

```java
try (ResultRows users = dt.select("select * from users")) {
    while (users.next()) {
        int userId = users.getInt("id");
        String userName = users.getString("name");
    }
}
```

POJOs and records are supported as well. The next example lets JDAX transform a `ResultRow` into a `User` object or record.

```java
try (ResultRows users = dt.select("select * from users")) {
    while (users.next()) {
        User user = users.get(User.class);

        int userId = user.id;
        String userName = user.name;
    }
}
```

* [Supported data types](docs/types.md)
* [More query variants](docs/queries.md)

### Inserting records

Basic insert statement:

```java
List<Long> inserted = dao.insertOne(
    values,
    "insert into numbers (scale, name) values (?, ?)",
    fieldName
);
```

The return value is a `List` containing the numeric identity of the newly inserted record.
The `fieldName` argument specifies which field values to return.

If `fieldName` is not provided, an insert count is returned instead.

Some databases do not support specifying the identity field explicitly.
In those cases, setting `Features.USE_GENERATED_KEYS_FLAG` can be used to return the default generated identity.

[More advanced queries](docs/queries.md)

### Updating records

Basic update statement:

```java
Object[] values = new Object[]{ "name", 127 };
int updated = dao.update(values, "update users set name = ? where id = ?");
```

This returns the number of updated records.
The `values` array contains the actual values for query input parameters.

[Advanced queries](docs/queries.md)
