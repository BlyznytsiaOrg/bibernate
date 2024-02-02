package testdata.manytomany.unidirectional;

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
    @JoinTable(name = "persons_courses", // join table annotation is in the owning side (Person)
        joinColumn = @JoinColumn(name = "person_id"), // persons foreign key in join table
        inverseJoinColumn = @JoinColumn(name = "course_id")) // courses foreign key in join table
    private List<Course> courses = new ArrayList<>();
    
}
