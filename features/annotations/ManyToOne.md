# @ManyToOne Annotation

The `@ManyToOne` annotation is used to specify a many-to-one relationship between two entities in Java.

## Attributes

1. **cascade** (Optional): Defines the cascade operations to be applied to the associated entity when operations are performed on the owning entity.

2. **fetch** (Optional): Defines the fetching strategy to be used when retrieving the associated entity.

## Usage

```java
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {

    /**
     * Defines the cascade operations to be applied to the associated entity when operations are performed on the owning entity.
     */
    CascadeType[] cascade() default {};

    /**
     * Defines the fetching strategy to be used when retrieving the associated entity.
     */
    FetchType fetch() default FetchType.EAGER;
}
```
## Examples

### Basic Usage
```java
import io.github.blyznytsiaorg.bibernate.annotation.*;
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
In this example, the Note entity class has a many-to-one relationship with the Person entity. By default, the cascade operations and fetching strategy are applied as per the default values (cascade = {} and fetch = FetchType.EAGER).

## Bidirectional relations
```java
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
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
    
    @OneToMany(mappedBy = "person")
    private List<Note> notes = new ArrayList<>();

}
```
```java
import io.github.blyznytsiaorg.bibernate.annotation.*;
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
In this example you can see bidirectional relations between Person and Note entities.

## Lazy fetch strategy

```java
import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    private Long id;

    private String text;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

}
```
```java
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
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

}
```
In this example there is a many-to-one relationship between Note and Person entities. The fetch = FetchType.LAZY attribute specifies that the associated Person entity should be lazily fetched, meaning it will not be loaded from the database until accessed

### See Also

- [@JoinColumn](JoinColumn.md)
- [Schema Generation](../SchemaGeneration.md)