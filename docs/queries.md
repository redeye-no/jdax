
# jdax: Advanced Query Features

The jdax library provides powerful features for dynamic SQL query handling, reducing the need for manual query modifications while ensuring efficiency and security.

---

## Parameter Expansion

Parameter expansion ensures that SQL statements automatically adjust based on input size.

### Example: Standard Query Issue

Consider the query:

```sql
SELECT * FROM numbers WHERE id IN (?, ?)
```

This query expects exactly 2 input values. If the input later has 4 values, the query would fail unless rewritten as:

```sql
SELECT * FROM numbers WHERE id IN (?, ?, ?, ?)
```

### Solution: Parameter Expansion in jdax

With jdax, queries dynamically adjust without modification.

```java
public ResultRows searchByIDs(Object[] ids) {
    return dao.select(ids, "SELECT * FROM numbers WHERE id IN (??)");
}
```

#### Usage Examples

This query remains unchanged while handling different input sizes:

```java
searchByIDs(new Object[]{1, 2});
searchByIDs(new Object[]{1, 2, 3, 4});
searchByIDs(new Object[]{10, 20, 30, 40, 50, 60});
```

No manual rewriting needed when input size changes.

---

## Parameter Replacement

Parameter replacement gives precise control over query input behavior.

### Example: Standard Insert Issue

```java
Number quadrillion = new Number(8, 15, "quadrillion");
InsertResults inserted = dao.insertOne(quadrillion, "INSERT INTO numbers (id, scale, name) VALUES (?, ?, ?)");
```

If `id` is an auto-generated identity field, this query fails.

### Solution: Skipping Auto-Generated Fields

Manually rewriting the query works:

```java
InsertResults inserted = dao.insertOne(quadrillion, "INSERT INTO numbers (scale, name) VALUES (?, ?)");
```

However, this approach is problematic because the value object (`quadrillion`) still has 3 values, which do not match the query.

### Using Parameter Replacement

With jdax, simply suppress the auto-generated field:

```java
Number quadrillion = new Number(8, 15, "quadrillion");
InsertResults inserted = dao.insertOne(quadrillion, "INSERT INTO numbers (scale, name) VALUES (#, ?, ?)");
```

#### How It Works

- The `#` skips the value at its position (`8`).
- The actual query executed:

    ```sql
    INSERT INTO numbers (scale, name) VALUES (?, ?)
    ```

Automatically removes mismatched values, ensuring correct SQL execution.

---

## Tagged Parameter Replacement

Tagged parameter replacement extends standard replacement by allowing direct SQL injection of tags.

### Example: Using a Sequence ID

```java
Number quadrillion = new Number(8, 15, "quadrillion");
InsertResults inserted = dao.insertOne(quadrillion,
    "INSERT INTO numbers (id, scale, name) VALUES (#SEQUENCE_ID.nextval, ?, ?)");
```

#### Translation to SQL

```sql
INSERT INTO numbers (id, scale, name) VALUES (SEQUENCE_ID.nextval, ?, ?)
```

### How It Works

- The `#` marker now includes a tag.
- Instead of suppressing the field, the tag replaces it with a valid SQL construct (such as `SEQUENCE_ID.nextval`).
- This allows using database-specific functions, such as:
  - Auto-increment fields
  - UUID generators
  - Custom SQL expressions

Supports database-specific constructs while keeping Java code clean.

---

## JDAX Auto-Scaling Feature

JDAX provides an auto-scaling feature for seamless numeric field conversion. This allows retrieving a BigDecimal field as an int without manual type conversion.

### Example: Auto-Scaling Conversion

```java
while (rows.next()) {
    BigDecimal bigPopulation = rows.getBigDecimal("big_decimal_field");
    int localPopulation = rows.getInt("big_decimal_field");
}
```

Here, `big_decimal_field` is retrieved as both a `BigDecimal` and an `int`, showcasing JDAX’s automatic conversion capabilities.

### Handling NULL Values

JDAX also provides a safeguard against `NULL` values using the `Features.NULL_RESULTS_DISABLED` flag.

- When enabled, JDAX automatically assigns default values to `NULL` fields.
- This eliminates the need for null checks in application code.

Benefit: Applications can safely handle database results without additional validation logic.

---

## Advanced Queries: Putting It All Together

jdax allows writing complex, dynamic queries with ease.

### Example: Complex Query with Expansion

```java
Object[] values = new Object[]{101, 18, "quintillion", 9};
Object[][] ins = new Object[][]{
    {1, 3, 5, 7, 9, 11, 13},
    {"sextillion", "septillion", "octillion"}
};
ResultRows selected = dao.select(values, ins, "
    SELECT * FROM numbers
    WHERE
        (id = ? OR scale < ?)
    OR
        (id IN (??) AND name NOT ?)
    OR
        (scale > ? AND name IN (??))");
```

### Automatic Expansion of `IN` Clauses

jdax automatically expands the `IN` placeholders (`??`):

```sql
SELECT * FROM numbers
WHERE
    (id = ? OR scale < ?)
OR
    (id IN (?, ?, ?, ?, ?, ?, ?) AND name NOT ?)
OR
    (scale > ? AND name IN (?, ?, ?))
```

#### How Inputs Are Handled

| Placeholder | Input Source | Example Value |
|-------------|-------------|--------------|
| `?` | `values[]` | `{101, 18, "quintillion", 9}` |
| `??` (first) | `ins[0]` | `{1, 3, 5, 7, 9, 11, 13}` |
| `??` (second) | `ins[1]` | `{"sextillion", "septillion", "octillion"}` |

### Final Expanded Query

```sql
SELECT * FROM numbers
WHERE
    (id = 101 OR scale < 18)
OR
    (id IN (1, 3, 5, 7, 9, 11, 13) AND name NOT 'quintillion')
OR
    (scale > 9 AND name IN ('sextillion', 'septillion', 'octillion'))
```

No manual query rewriting needed when input sizes change.

---

# Summary of jdax Advanced Features

| Feature | Benefit |
|---------|---------|
| Parameter Expansion | Eliminates manual query rewrites when input sizes change. |
| Parameter Replacement | Allows skipping fields dynamically. |
| Tagged Parameter Replacement | Enables injecting database-specific constructs. |
| Complex Query Expansion | Handles multi-value inputs efficiently. |

jdax simplifies SQL handling, making queries adaptive, scalable, and clean!

[Main documentation](../README.md)
