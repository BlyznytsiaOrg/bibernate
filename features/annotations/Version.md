## @Version Annotation

Indicates that the annotated field represents a version attribute used for optimistic locking. Optimistic locking allows multiple transactions to access the same data simultaneously with the assumption that conflicts are rare. When an entity is updated, the version attribute is checked to detect concurrent modifications.

### Example usage:

```java
@Getter
@Setter
@Entity
@Table(name = "employees")
public class EmployeeEntity {
    @Id
    private Long id;

    private String firstName;

    private String lastName;

    @Version
    private Integer version;
}
```


In this example, the version field of the EmployeeEntity class is annotated with @Version. 
When instances of EmployeeEntity are updated, the value of the version field is automatically incremented, 
allowing the system to detect concurrent modifications and prevent data loss.

### See Also

- [Versioning](../Versioning.md)