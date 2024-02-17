# @Table Annotation

The `@Table` annotation is used to specify the mapping of an entity class to a database table in Java.

## Attributes

1. **name** (Optional): Specifies the name of the table in the database. If not specified, the entity class name is used as the table name.

2. **indexes** (Optional): Specifies indexes for the table. These indexes are used when table generation is in effect. Note that it is not necessary to specify an index for a primary key, as the primary key index will be created automatically.

## Usage

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

     /**
     * Specifies the name of the table.
     *
     * @return the name of the table
     */
    String name() default "";

    /**
     * (Optional) Indexes for the table.  These are only used if
     * table generation is in effect.  Note that it is not necessary
     * to specify an index for a primary key, as the primary key
     * index will be created automatically.
     *
     * @return an array of indexes for the table
     */
    Index[] indexes() default {};
}
```

## Examples
### Basic Usage
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    private Long id;

    // Entity fields and methods
}
```
In this example, the Employee entity class is mapped to a database table named "employees". Since the table name is explicitly specified using the name attribute of the @Table annotation, the default entity class name is not used as the table name.

### Table with Indexes
```java
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_name", columnList = "name"),
        @Index(name = "idx_employee_department", columnList = "department_id")
})
public class Employee {
    @Id
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    // Entity fields and methods
}
```
In this example, the Employee entity class is mapped to a database table named "employees" with two indexes: one on the "name" column and another on the "department_id" column.

### See Also

- [Schema Generation](../SchemaGeneration.md)