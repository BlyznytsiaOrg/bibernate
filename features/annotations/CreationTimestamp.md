# CreationTimestamp Annotation

Indicates that the annotated field should be automatically populated with the timestamp of entity creation.

### Example usage:

```java
public class ExampleEntity {
    @CreationTimestamp
    private OffsetDateTime createdAt;

    // Getter and setter methods
}
```
### See Also

- [@UpdateTimestamp](UpdateTimestamp.md)
- [Schema Generation](../SchemaGeneration.md)
- [Runtime Entity Validation](../RuntimeEntityValidation.md) 