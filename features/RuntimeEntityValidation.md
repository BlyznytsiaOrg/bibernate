## Runtime Entity Validation

- *The Bibernate* utilize the `EntityMetadataCollector` class to gather all entities using reflection and subsequently utilize the collected data.
    Then print the number of entities found. If you don't have any entities you will see and errors.

```
15:56:42.588 [main] TRACE i.g.b.b.e.m.EntityMetadataCollector - Found entities size 0
Exception in thread "main" io.github.blyznytsiaorg.bibernate.exception.EntitiesNotFoundException: Cannot find any entities on classpath with this package com.levik.bibernate.demo.entity
at io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector.collectMetadata(EntityMetadataCollector.java:80)
at io.github.blyznytsiaorg.bibernate.Persistent.<init>(Persistent.java:93)
at io.github.blyznytsiaorg.bibernate.Persistent.withDefaultConfiguration(Persistent.java:44)
at com.levik.bibernate.demo.BibernateDDLDemoApplication.main(BibernateDDLDemoApplication.java:13)
```

### Also several checks are completed during gathering metadata information:
- When there is a mismatch between name of index columnList and column name

```java
@Table(name = "notes", indexes = {@Index(columnList = "descr")})
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String description;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: Error generating index for table 'notes' [column 'descr' does not exist]
```

- When `@JoinTable` annotation is set on field without `@ManyToMany` annotation
```java
@Entity
public class TestOne {
    @Id
    private Long id;

    @JoinTable(name = "test1_test2", joinColumn = @JoinColumn(name = "test1_id"),
            inverseJoinColumn = @JoinColumn(name = "test1"))
    List<TestTwo> testTwos = new ArrayList<>();
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: No @ManyToMany annotation is set in class 'TestOne' on field 'testTwos' annotated with @JoinTable annotation
```
- When there is no `@ManyToOne` of `@OneToOne` annotation on field annotated with `@JoinColum` annotation
```java
@Entity
@IgnoreEntity
public class TestOne {

    @Id
    private Long id;

    @JoinColumn(name = "test_two_id")
    private TestTwo testTwo;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: No @OneToOne or @ManyToOne annotation on field 'testTwo' annotated with @JoinColumn
```
-When field annotated with `@OneToOne` annotation with mappedBy that does not have relation
```java
@Entity
public class TestOne {
    @Id
    private Long id;

    @OneToOne(mappedBy = "testOne")
    private TestTwo testTwo;
}

@Entity
public class TestTwo {
    @Id
    private Long id;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: Can't find in entity 'TestTwo' @OneToOne annotation as entity 'TestOne' is annotated with @OneToOne mappedBy='testOne'
```
- When field annotated with `@ManyToMany` annotation with mappedBy that does not have relation
```java
@Entity
public class TestOne {
    @Id
    private Long id;

    @ManyToMany(mappedBy = "testOnes")
    private List<TestTwo> testTwos = new ArrayList<>();
}

@Entity
public class TestTwo {
    @Id
    private Long id;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: Can't find in entity 'TestTwo' @ManyToMany annotation as entity 'TestOne' is annotated with @ManyToMany mappedBy='testOnes'
```
- When field is annotated both `@CreationTimestamp` and `@UpdateTimestamp` annotations
```java
@Entity
public class Test {
    @Id
    private Long id;

    @CreationTimestamp
    @UpdateTimestamp
    private LocalDateTime createdAt;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: In class 'Test' on field 'createdAt' can't be @CreationTimestamp and @UpdateTimestamp annotations simultaneously
```

- When type of the field not sufficient for `@CreationTimestamp` and `@UpdateTimestamp` annotations. Supported types are `LocalDate`, `OffsetTime`, `LocalTime`, `OffsetDateTime`, `LocalDateTime`.
```java
@Entity
public class Test {
    @Id
    private Long id;

    @CreationTimestamp
    private String time;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: In class 'Test' field 'time' with type 'String' is not supported for @CreationTimestamp or @UpdateTimestamp annotations
```

- When type of the field not sufficient for `@CreationTimestamp` and `@UpdateTimestamp` annotations
```java
@Entity
public class Test {
    @Id
    private Long id;

    @CreationTimestamp
    private String time;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: In class 'Test' field 'time' with type 'String' is not supported for @CreationTimestamp or @UpdateTimestamp annotations
```

- When field that is relation is annotated with `@Column` annotation
```java
@Entity
public class Test {
    @Id
    private Long id;

    @OneToOne
    @Column
    private TestTwo testTwo;
}
```
```
io.github.blyznytsiaorg.bibernate.exception.MappingException: The @Column annotation can not be used on relation field 'testTwo' in class 'Test'
```