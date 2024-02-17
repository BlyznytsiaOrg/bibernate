# Index Annotation

Indicates that the annotated entity should have an index created on specified columns.

### Example usage:

```java
@Entity
@Table(indexes = {@Index(name = "phone_idx", columnList = "companyNumber")})
public class Phone {
    @Id
    @GeneratedValue
    private Long id;
    
    private String companyNumber;
}
```
### See Also

- [@Table](Table.md)
- [Schema Generation](../SchemaGeneration.md)