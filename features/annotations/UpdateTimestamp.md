# UpdateTimestamp Annotation

Indicates that the annotated field represents a timestamp that should be updated automatically upon entity modification.

### Example usage:

```java
public class ExampleEntity {
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Getter and setter methods
}
```

### See Also

- [@CreationTimestamp](CreationTimestamp.md)
- [Schema Generation](../SchemaGeneration.md)
- [Runtime Entity Validation](../RuntimeEntityValidation.md) 