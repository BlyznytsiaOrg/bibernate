# ForeignKey Annotation

Indicates that the annotated field represents a foreign key constraint in a database table.

### Example usage:

```java
@Entity
public class Phone {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "author_profile_id", foreignKey = @ForeignKey(name = "FK_phone_author_profile"))
    private AuthorProfile authorProfile;
}
```
### See Also

- [@JoinColumn](JoinColumn.md)
- [@JoinTable](JoinTable.md)
- [Schema Generation](../SchemaGeneration.md)