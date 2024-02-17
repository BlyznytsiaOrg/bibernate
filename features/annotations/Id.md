# Id Annotation

Marks a field as the identifier (primary key) of an entity class. This annotation is typically applied to a field within a Java
class that represents an entity in a data store. The presence of this annotation signals that the annotated field is 
used to uniquely identify instances of the entity class.

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
- [@Entity](Entity.md)
- [Schema Generation](../SchemaGeneration.md)