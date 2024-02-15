# @Column Annotation

The `@Column` annotation is used to specify the mapping of a persistent property or field to a column in the database table.

## Attributes

1. **name** (Optional): Specifies the name of the column in the database table. If not specified, the property or field name is used as the column name.

2. **unique** (Optional): Indicates whether the column is a unique key constraint. By default, it is set to `false`.

3. **nullable** (Optional): Specifies whether the column allows null values. By default, it is set to `true`.

4. **columnDefinition** (Optional): Provides a SQL fragment that is used when generating the DDL for the column. This can be used to specify additional column attributes or constraints.

## Usage

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * (Optional) The name of the column in the database table.
     *
     * @return The name of the column.
     */
    String name() default "";

    /**
     * (Optional) Whether the column is a unique key.
     *
     * @return Whether the column is a unique key.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the database column is nullable.
     *
     * @return Whether the column allows NULL values.
     */
    boolean nullable() default true;

    /**
     * (Optional) The SQL fragment that is used when generating the DDL for the column.
     *
     * @return The custom SQL fragment.
     */
    String columnDefinition() default "";
}
```

## Examples

### Basic Usage
```java
import io.github.blyznytsiaorg.bibernate.annotation.*;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    private Long id;

    @Column(name = "employees_name", unique = true, nullable = false)
    private String name;

    // Other fields and methods
}
```
In this example, the name property of the Employee class is mapped to a column named "employees_name" in the database table. The column is marked as unique and not nullable.

### Column Definition
```java
import io.github.blyznytsiaorg.bibernate.annotation.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private Long id;

    @Column(name = "products_name", columnDefinition = "VARCHAR(255) NOT NULL UNIQUE")
    private String productName;

    // Other fields and methods
}
```
In this example, the productName property of the Product class is mapped to a column with a custom column definition specifying that it should be a VARCHAR(255) column that is not nullable and has a unique constraint.