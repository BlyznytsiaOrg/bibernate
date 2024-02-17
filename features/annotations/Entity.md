# Entity Annotation

Marks a class as an entity for mapping in the Bibernate framework. This annotation is typically applied to a class within a Java class hierarchy that represents entities in a data store. When present, 
instances of the annotated class will be considered entities during entity scanning and mapping processes.

## Examples

### Basic Usage
```java
@Entity
public class Person {
    @Id
    private Long id;
}
```

### See also:
- [@Id](Id.md)
- [Schema Generation](../SchemaGeneration.md)