# Schema Generation

To use auto Schema Generation the property `bibernate.2ddl.auto=create` should be set in the `bibernate.properties` file located in `resource` folder. Schema Generation support PostgresSQL database.
```properties
db.url=
db.user=
db.password=
bibernate.2ddl.auto=create
```
**NOTE:** If both the `bibernate.flyway.enabled=true` and `bibernate.2ddl.auto=create` properties are set in the `bibernate.properties` file, then a `BibernateGeneralException` will be thrown, indicating that only one of these properties can be set.



The minimum requirement to generate a schema is to include the `@Entity` annotation on the class and annotate the field that will serve as the primary key in the database with `@Id`.
```java
@Entity
public class Person {
    @Id
    private Long id;
}
```

The table name can be customized by using the `@Table` annotation with the name parameter:
```java
@Entity
@Table(name ="persons")
public class Person {
    @Id
    private Long id;
}
```

For creating indexes in database in `@Table` annotation `indexes` attribute can be used.
```java
@Entity
@Table(indexes = {@Index(name = "phone_idx", columnList = "companyNumber")})
public class Phone {
    @Id
    @GeneratedValue
    private Long id;
    
    private String companyNumber;
}
```
**NOTE:** Please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due mismatch between name of index columnList and column name.

For id generation `@GeneratedValue` annotation can be used. It allows the specification of a generation strategy 
for the annotated field, indicating how the database generates the values for this field. There are three generation strategies: `IDENTITY`, `SEQUENCE` and `NONE`.
The default generation strategy is `GenerationType.IDENTITY`.
```java
@Entity
@Table(name ="persons")
public class Person {
    
    @Id
    @GeneratedValue
    private Long id;
}
```
**NOTE:** The Schema generation supports `java.lang.Integer`, `int`, `java.lang.Long`, `long` types for identifier field annotated with `@GeneratedValue`, otherwise `UnsupportedDataTypeException` will be thrown. 


For generation strategy `SEQUENCE`, the `@SequenceGenerator` annotation could be used. The `name` attribute defines the name of the generator, while the `sequenceName` attribute specifies the name of the database sequence to be used. The `initialValue` and `allocationSize` attributes allow 
customization of the initial value and the increment size of the sequence.
```java
@Entity
@Table(name ="persons")
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_gen")
    @SequenceGenerator(name = "person_gen", sequenceName = "person_seq", initialValue = 10, allocationSize = 20)
    private Long id;
}
```
**NOTE:** Please refer to [Annotation Processing Entity Validation](AnnotationProcessing.md) for compile errors that may occur due to inconsistencies in generator names between the `@GeneratedValue` and `@SequenceGenerator` annotations.

The `@Column` annotation is used to map a Java class field to a database column. 
It allows customization of various column properties such as name, uniqueness, nullability and column definition.
```java
@Entity
@Table(name ="persons")
public class Person {
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "passport_data", unique = true, nullable = false, columnDefinition = "VARCHAR(50)") 
    private String passportData;
}
```
**NOTE:** Please refer [here](SupportedTypesForDDL.md) on the Java types that are supported for Schema Generation.
Also, please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due to annotating field with `@Column` annotation that is relation.

The `@CreationTimestamp` annotation is uses on field that should be automatically populated with the timestamp of entity creation and `@UpdateTimestamp` annotation uses on field represents a timestamp that should be updated automatically upon entity modification.

```java
import java.time.OffsetDateTime;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    private OffsetDateTime createdAt;
    
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
```
**NOTE:** Please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due to insufficient type or simultaneous use of both annotations on one field. 

The `OneToOne` annotation specifies a one-to-one relationship between two entities. This annotation is used to configure the mapping between the owning side and the inverse side of the relationship.
```java
@Entity
@Table(name ="persons")
public class Person {
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "passport_data", unique = true, nullable = false, columnDefinition = "VARCHAR(50)") 
    private String passportData;
    
    @OneToOne(mappedBy = "person")
    private PersonProfile personProfile;
}

@Entity
@Table(name ="person_profiles")
public class PersonProfile {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Person person;
}
```
**NOTE:** Please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due to field annotated with `@OneToOne` annotation with mappedBy does not have relation.

The `@JoinColumn` specifies a column for joining an entity association. It allows customization column properties like column name and foreign key constraint for the column. 
```java
@Entity
@Table(name ="person_profiles")
public class PersonProfile {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_person_profile_author"))
    private Person person;
}
```
**NOTE:** Please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due the field annotated with  `@JoinColum` annotation without `@OneToOne` or `@ManyToOne` annotation.

The `@ManyToOne` and `@OneToMany` annotations specify the corresponding relations.
```java
@Entity
public class AuthorProfile {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "author")
    private List<Phone> phones = new ArrayList<>();
}

@Entity
public class Phone {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private AuthorProfile authorProfile;
}
```

For Many-to-Many relations `@JoinTable` annotation can be used.  When using this annotation the name of the join table, the join column, 
the inverse join column should be specified and optionally, foreign key constraints for both columns.
```java
@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    @JoinTable(name = "books_authors", joinColumn = @JoinColumn(name = "book_id"),
            inverseJoinColumn = @JoinColumn(name = "author_id"),
            foreignKey = @ForeignKey(name = "FK_book_book_authors"),
            inverseForeignKey = @ForeignKey(name = "FK_authors_book_authors"))
    List<Author> authors = new ArrayList<>();
}
```
**NOTE:** Please refer to [Runtime Entity Validation](RuntimeEntityValidation.md) for runtime errors that may occur due to use of `@JoinTable` annotation on field that does not have `@ManyToMany` annotation.


- Entity Metadata package [Java Doc](https://blyznytsiaorg.github.io/bibernate-core-javadoc/io/github/blyznytsiaorg/bibernate/entity/metadata/package-summary.html)
- DDL package [Java Doc](https://blyznytsiaorg.github.io/bibernate-core-javadoc/io/github/blyznytsiaorg/bibernate/ddl/package-summary.html)

### See Also

- [Flyway Migration Support](FlywayMigrationSupport.md)