package testdata.manytomany.bidirectional.error;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    private Long id;
    
    private String name;
    
    @ManyToMany // mappedBy is missing
    private List<Person> persons = new ArrayList<>();
    
}
