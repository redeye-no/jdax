# Markdown for Netbeans ![Description Here](https://raw.githubusercontent.com/moacirrf/netbeans-markdown/main/images/nblogo48x48.png)

***


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

| Feature | Flag |
|---|---|
| `USE_GENERATED_KEYS_FLAG` | DataSource supports Statement.RETURN_GENERATED_KEYS to retrieve ID of newly inserted rows |
| `NULL_RESULTS_DISABLED` | Returns default values for NULL fields in the DB |
| `AUTO_COMMIT_ENABLED` | Set connection to auto-commit mode |
| `AUTO_COMMIT_DISABLED` | Disable connection auto-commit |
| `READ_ONLY_MODE` | Put connection in read-only mode |

Example:

```java
DataSource dataSource = new HikariDataSource(new new HikariConfig());
Connector.prepare("ds-users", dataSource, Features.AUTO_COMMIT_ENABLED);
```

With this connection, queries will be committed automatically.
The API allows for multiple features to be specified together.

```java
Connector.prepare("ds-users", dataSource, 
    Features.AUTO_COMMIT_ENABLED, 
    Features.USE_GENERATED_KEYS_FLAG,
    Features.NULL_RESULTS_DISABLED);
```

[Main documentation](../README.md)
