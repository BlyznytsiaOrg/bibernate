# @OneToMany Annotation

The `@OneToMany` annotation is used to specify a one-to-many relationship between two entities in Java.

## Attributes

1. **mappedBy** (Optional): Specifies the field in the target entity that owns the relationship. This is required unless the relationship is unidirectional.

2. **cascade** (Optional): Defines the cascade operations to be applied to the associated entities when operations are performed on the owning entity.

## Usage

```java
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {

    /**
     * The field that owns the relationship. Required unless the relationship is unidirectional.
     * 
     * @return the field that owns the relationship
     */
    String mappedBy() default "";

    /**
     * Defines the cascade operations to be applied to the associated entities when operations are performed on the owning entity.
     *
     * @return the cascade operations to be applied
     */
    CascadeType[] cascade() default {};
}
```

## Examples
### Basic Usage

```java
import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "persons")
public class Person {

    @Id
    private Long id;

    private String firstName;

    private String lastName;

    @OneToMany
    @JoinColumn(name = "person_id") // This column is in the Notes table
    private List<Note> notes = new ArrayList<>();
}
```
The association between Person and Note is established through a one-to-many relationship, where the foreign key column "person_id" in the "notes" table links back to the Person entity.

## Bidirectional relations
```java
import java.util.ArrayList;
import java.util.List;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "persons")
public class Person {
    @Id
    private Long id;

    private String firstName;

    private String lastName;

    @OneToMany(mappedBy = "person")
    private List<Note> notes = new ArrayList<>();

}
```
```java
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    private Long id;

    private String text;
    
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

}
```
In this example, the Person entity class has a one-to-many relationship with the Note entity. The mappedBy = "person" attribute specifies that the Person entity owns the relationship via its notes field.

- [Schema Generation](../SchemaGeneration.md)
