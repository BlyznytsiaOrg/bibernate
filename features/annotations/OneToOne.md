# Custom OneToOne Annotation

The `OneToOne` annotation is used to define a one-to-one relationship between two entities in Java.

## Attributes

1. **mappedBy** (Optional): Specifies the name of the field in the inverse side of the relationship. This is used to map bidirectional associations. If not specified, it indicates that the relationship is unidirectional.

2. **cascade** (Optional): Defines the cascade behavior for the relationship. It determines how operations on the owning entity affect the associated entity. By default, no cascading behavior is applied.

3. **fetch** (Optional): Specifies the fetching strategy used to load the related entity. It determines when the associated entity should be loaded from the database. By default, eager fetching is used, meaning that the associated entity is loaded immediately along with the owning entity.

## Usage

```java
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {

    /**
     * The name of the field in the inverse side of the relationship.
     * This is required unless the relationship is unidirectional.
     * Defaults to an empty string, indicating that the relationship is not mapped by another field.
     */
    String mappedBy() default "";

    /**
     * Defines the cascade behavior for the relationship.
     * By default, no cascading behavior is applied.
     */
    CascadeType[] cascade() default {};

    /**
     * Defines the fetching strategy used to load the related entity.
     * By default, eager fetching is used.
     */
    FetchType fetch() default FetchType.EAGER;
}
