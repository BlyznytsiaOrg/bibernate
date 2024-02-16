# @ManyToMany Annotation

The `@ManyToMany` annotation is used to specify a many-to-many relationship between two entities in Java.

## Attributes

1. **mappedBy** (Optional): Specifies the name of the field on the target entity that owns the relationship. This is used to establish the owning side of the many-to-many relationship.

2. **cascade** (Optional): Specifies the cascade operations to be applied to the relationship, indicating how changes in one entity should affect the other entity.

## Usage

```java
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {

    /**
     * (Optional) The name of the field on the target entity that owns the relationship.
     *
     * @return The name of the field that owns the relationship.
     */
    String mappedBy() default "";

    /**
     * (Optional) Specifies the cascade operations to be applied to the relationship.
     *
     * @return The cascade operations.
     */
    CascadeType[] cascade() default {};
}
```

## Examples
### Basic Usage
```java
public class Student {

    @ManyToMany
    private List<Course> courses;

    // Other fields and methods
}
```
In this example, the Student entity class has a many-to-many relationship with the Course entity. By default, the @ManyToMany annotation is used without specifying any additional attributes.

### Owning Side of the Relationship
```java
public class Course {

    @ManyToMany(mappedBy = "courses")
    private List<Student> students;

    // Other fields and methods
}
```
In this example, the Course entity class also has a many-to-many relationship with the Student entity. The mappedBy = "courses" attribute specifies that the Student entity owns the relationship via its courses field.

### More detailed Bidirectional example
```java
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    private Long id;
    
    private String name;
    
    @ManyToMany(mappedBy = "courses")
    private List<Person> persons = new ArrayList<>();
    
}
```

```java
import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    @ManyToMany
    @JoinTable(name = "persons_courses", // join table annotation is in the owning side (Person)
        joinColumn = @JoinColumn(name = "person_id"),
        inverseJoinColumn = @JoinColumn(name = "course_id"))
    private List<Course> courses = new ArrayList<>();
    
}
```
There is many-to-many bidirectional relationship between Course and Person entities. Person entity owns the relationship via courses field.