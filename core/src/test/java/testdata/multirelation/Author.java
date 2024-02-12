package testdata.multirelation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(exclude = "courses")
@Entity
@Table(name = "authors")
public class Author {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "author")
    private List<Course> courses;
    
}
