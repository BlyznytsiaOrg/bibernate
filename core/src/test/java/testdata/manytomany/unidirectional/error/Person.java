package testdata.manytomany.unidirectional.error;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    @ManyToMany
    @JoinTable(name = "persons_courses",
        joinColumn = @JoinColumn(name = "person_id"),
        inverseJoinColumn = @JoinColumn(name = "courseeeeee_id"))
    private List<Course> courses = new ArrayList<>();
    
}
