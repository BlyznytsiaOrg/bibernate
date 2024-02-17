# JoinTable Annotation

Specifies a join table for defining a many-to-many association between two entities.

When using this annotation, you must specify the name of the join table, the join column, the inverse join column, and optionally, foreign key constraints for both columns.

The join table should only be specified on fields representing many-to-many associations, and it cannot be used for other types of associations (e.g., one-to-many, many-to-one).

## Examples
### Basic Usage
```java
@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    @JoinTable(name = "books_authors", joinColumn = @JoinColumn(name = "book_id"),
            inverseJoinColumn = @JoinColumn(name = "author_id"),
            foreignKey = @ForeignKey(name = "FK_book_book_authors"),
            inverseForeignKey = @ForeignKey(name = "FK_authors_book_authors"))
    List<Author> authors = new ArrayList<>();
}
```

### See also:
- [@ManyToMany](ManyToMany.md)
- [Schema Generation](../SchemaGeneration.md)