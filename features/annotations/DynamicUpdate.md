## @DynamicUpdate Annotation

Indicates that the annotated entity should use dynamic update behavior, where only modified fields are included in the SQL update statement.


### Example usage:

```java
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonWithoutDynamicUpdate.class, 1L).orElseThrow();
                firstName = person.getFirstName();
                lastName = person.getLastName();
                person.setFirstName(person.getFirstName() + uuid);
            }
```


### Result of queries:
```bash
"SELECT * FROM persons WHERE id = ?;",
"UPDATE persons SET first_name = ?, last_name = ? WHERE id = ?;")
```