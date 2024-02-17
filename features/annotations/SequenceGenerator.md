# @SequenceGenerator Annotation

The `@SequenceGenerator` Specifies a sequence generator for generating values for annotated fields marked with
@GeneratedValue annotation using the GenerationType.SEQUENCE strategy.
This annotation is typically applied to a field, method, or class within a Java class that represents an entity in a data store.

## Attributes

1. **name** (Required): Defines the name of the generator. 
    Bibernate matches @SequenceGenerator with @GeneratedValue which have the same generator parameter as sequence generator name.

2. **sequenceName** (Required): Specifies the name of the database sequence to be used.

3. **initialValue** (Optional): Specifies the initial value for the sequence. The default is 1.

4. **allocationSize** (Optional): Specifies the size of the allocation block for the sequence. The default is 50.


## Usage

```java
@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATOR_PERSON")
    @SequenceGenerator(name = "SEQ_GENERATOR_PERSON", sequenceName = "person_id_custom_seq", initialValue = 5, allocationSize = 5)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
}
```
In the example above we specified the name of the generator in the @GeneratedValue annotation, and it matches 
the name attribute in the @SequenceGenerator annotation.
initialValue and allocationSize mean that our sequence will start from 5 and to generate the next 5 identifiers 
only one query will be made to the database.



