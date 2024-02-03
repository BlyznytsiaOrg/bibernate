package testdata.multirelation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.GenerationType;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(exclude = "courses")
@Entity
@Table(name = "authors")
public class Author {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "author")
    private List<Course> courses;
    
}
