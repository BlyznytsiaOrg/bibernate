# Criteria API

## SelectQueryBuilder

The SelectQueryBuilder is a SQL SELECT query builder for constructing SELECT statements with optional clauses such as JOIN, WHERE, GROUP BY, HAVING, and UNION.

**Example Usage:**

```java
    var selectQueryBuilder = SelectQueryBuilder.from("users")
        .selectField("name")
        .selectField("age")
        .join("orders", "users.id = orders.user_id", JoinType.LEFT)
        .whereCondition("age > ?")
        .groupBy("name")
        .havingCondition("COUNT(*) > 1");
    
    String sql = selectQueryBuilder.buildSelectStatement();
```

**This will generate the following SQL statement:**

```java

    SELECT name, age 
    FROM users 
    LEFT JOIN orders ON users.id = orders.user_id 
    WHERE age > ? 
    GROUP BY name 
    HAVING COUNT(*) > 1;

```


In addition to the SelectQueryBuilder, we also provide CRUD query builders for INSERT, UPDATE, and DELETE operations:


- InsertQueryBuilder: Builds SQL INSERT statements.
- UpdateQueryBuilder: Builds SQL UPDATE statements.
- DeleteQueryBuilder: Builds SQL DELETE statements.

## InsertQueryBuilder


```java
var insertQueryBuilder = InsertQueryBuilder.from("users")
        .setField("name")
        .setField("age");

String sql = insertQueryBuilder.buildInsertStatement();

```


**This will generate the following SQL statement:**

```java
INSERT INTO users (name, age) VALUES (?, ?);
```

**Note:**

- Ensure to call the setField method for each field that you want to include in the INSERT statement.
- The generated SQL statement uses parameter placeholders (?) to indicate where the actual values should be bound.

## UpdateQueryBuilderTest


```java

String query = UpdateQueryBuilder.update("users")
                .setField("age", "?")
                .whereCondition("id = ?")
                .buildUpdateStatement();

```

**This will generate the following SQL statement:**

```java
UPDATE users SET age = ? WHERE id = ?;
```


## DeleteQueryBuilder

```java

var query = DeleteQueryBuilder.from("users")
    .whereCondition("age > ?")
    .andCondition("enabled = ?")
    .buildDeleteStatement();

```


**This will generate the following SQL statement:**

```java
DELETE FROM users WHERE age > ? AND enabled = ?;
```