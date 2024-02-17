# IgnoreEntity Annotation

Marks a class as being ignored for entity mapping in the Bibernate framework. This annotation is typically applied to a
class within a Java class hierarchy that represents entities in a data store. 
When present, instances of the annotated class will not be treated as entities during entity scanning and mapping processes.

## Examples

### Basic Usage
```java
@IgnoreEntity
public class Person {
    @Id
    private Long id;
}
```

### See also:
- [@Entity](Entity.md)