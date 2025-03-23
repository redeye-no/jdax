
# Establishing a Connection

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

### Using a DataSource

```java
    private synchronized DataSource dataSource() {
        return new HikariDataSource(config());
    }

    private HikariConfig config() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        config.setMaximumPoolSize(8);
        config.setMinimumIdle(2);
        config.setJdbcUrl("jdbc:derby:memory:jdaxdb;create=true");
        return config;
    }
```

```java
Connector.prepare("MY_DATASOURCE", dataSource());
```

### Using a DataSourceFunction

```java
        DataSource dataSource = dataSource();

        Function<String, DataSource> dsCreator = new Function<String, DataSource>() {
            @Override
            public DataSource apply(String t) {
                return dataSource;
            }
        };

        Connector.prepare("MY_DATASOURCE", dsCreator);
```

## Features

`Features` are used for setting various connection flags.

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
