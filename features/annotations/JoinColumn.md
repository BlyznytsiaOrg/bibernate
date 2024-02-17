# @JoinColumn Annotation

The `@JoinColumn` annotation is used to specify the mapping of a persistent property or field as a foreign key column in a database table.

## Attributes

1. **name** (Required): Specifies the name of the column in the database table.

2. **foreignKey** (Optional): Specifies the foreign key constraint for the column.

## Usage

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {

    /**
     * Specifies the name of the column.
     *
     * @return the name of the column
     */
    String name();

    /**
     * Specifies the foreign key constraint for the column.
     *
     * @return the foreign key constraint for the column
     */
    ForeignKey foreignKey() default @ForeignKey(name = "");
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
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // Other fields and methods
}
```
In this example, the Employee entity class has a department field mapped as a foreign key column named "department_id" in the database table.

### Join Column with Foreign Key Constraint
```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_order_customer"))
    private Customer customer;

    // Other fields and methods
}
```
In this example, the Order entity class has a customer field mapped as a foreign key column named "customer_id" in the database table. Additionally, a foreign key constraint named "fk_order_customer" is specified for the column.

### See Also

- [@OneToOne](OneToOne.md)
- [@ManyToOne](ManyToOne.md)
- [Schema Generation](../SchemaGeneration.md)
