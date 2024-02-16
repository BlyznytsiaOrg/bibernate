# Batch Processing

The batch processing feature in Bibernate facilitates the efficient handling of database operations by minimizing
round-trips to the database. This results in improved performance, especially when dealing with large datasets.

## `saveAll` Method

### Description:

The `saveAll` method allows for batch insertion of entities into the database.

### Signature:

```java
public<T> void saveAll(Class<T> entityClass,Collection<T> entities);
```

### Example:
```java
List<User> userList= //... create a list of User entities
bibernateSession.saveAll(User.class,userList);
```

## `deleteAllById` Method

### Description:

The `deleteAllById` method enables batch deletion of entities by their primary keys.

### Signature:

```java
public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys);
```

### Example:
```java
List<Long> userIdsToDelete = //... create a list of User IDs to delete
bibernateSession.deleteAllById(User.class, userIdsToDelete);
```

## `deleteAll` Method

### Description:

The `deleteAll` method supports batch deletion of entities based on a collection of entity instances.

### Signature:

```java
public <T> void deleteAll(Class<T> entityClass, Collection<T> entities);
```

### Example:
```java
List<User> usersToDelete = //... create a list of User entities to delete
bibernateSession.deleteAll(User.class, usersToDelete);
```

These batch processing methods provide a convenient way to optimize database operations by grouping them into batches,
resulting in improved overall performance.