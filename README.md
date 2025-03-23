
# jdax

A data access layer on top of JDBC for implementing CRUD operation.

JDAX is a Java library that simplifies data access operations by providing an abstraction layer over JDBC. 
It streamlines CRUD operations through a consistent API for data retireval and management.

## Features

- Simplified JDBC connection configuration
- Consistent API for CRUD operations
- Effective SQL syntax safe from injection attacks
- Advanced query augmentation
- Almost seamless integration with existing JDBC code
- Support for both basic data types (ints, and Strings) to comples objects and records

## Installation

    <dependency>
        <groupId>no.redeye</groupId>
        <artifactId>jdax-lib</artifactId>
    </dependency>

## Programming against the library API

The following objects make up the basic framework:

|Class	|Description|
|-------|-----------|
| `Connector`	| Provides a mechanism for managing JDBC connections. |
| `DAOType`	| Access class providing read, write and delete operations. |
| `Features`	| Flags for configuring the behaviour of the connections. |
| `VO`          | A type definition for data access and value objects. |
| `ResultRows`  | A return type with DB query results. |

# Usage

## Establishing a Connection

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

Example:

```java
DataSource dataSource = new HikariDataSource(new new HikariConfig());
Connector.prepare("ds-users", dataSource, Features.AUTO_COMMIT_ENABLED);
```

[More details on establishing connections](docs/connections.md)

## Running queries

The `DAOType` manages query executions, and transforms resultsets.
Applications can either call `DAOType` directly, or inherit from it in implementing custom data access objects (DAOs).

```java
DAOType dt = new DAOType("ds-users");
ResultRows users = dt.select("select * from users");
```

### Selecting data

Queries' result sets are always returned in a ResultRows wrapper object that provides a jdax API for retrieving data.

```java
try (ResultRows users = dt.select("select * from users")) {
  while (users.next()) {
    int userId = users.getInt("id");
    int userName = users.getString("name");
  }
}
```

POJOs and records are supported as well. The next example lets jdax transform a ResultRow in to an User object/record

```java
try (ResultRows users = dt.select("select * from users")) {
  while (users.next()) {
    User user = users.get(User.class);

    int userId = user.id;
    int userName = user.name;
  }
}
```

[Supported data types](docs/types.md)
[More query variants](docs/queries.md)


### Inserting records

Basic insert statement

```java
List<Long> inserted = dao.insertOne(values, "insert into numbers (scale, name) values (?, ?)", fieldName);
```

The return value is a List of the numeric identity of the newly inserted record. 
The `fieldName` argument specifies what field values to return.
If fieldName is not provided, then an insert count is returned.

Some databases do not support the use of `fieldName` to specify the identity field. Setting the `Features.USE_GENERATED_KEYS_FLAG`
can be used for those implementations, and the default identity will be returned instead.

[More advanced queries](docs/queries.md)

#### Updating records

Basic update statement

```java
Object[] values = new Object[]{ "name", 127};
int updated = dao.update(values, "update users set name = ?, where id = ?)");
```

This returns the number of updated records. The `values` array contains the actual values for query input parameters.

[Advanced queries](docs/queries.md)
