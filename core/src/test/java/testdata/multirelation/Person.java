package testdata.multirelation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.GenerationType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"notes", "courses"})
@Data
@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String firstName;
    
    private String lastName;

    @OneToMany(mappedBy = "person")
    private List<Note> notes = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(name = "persons_courses", // join table annotation is in the owning side (Person)
        joinColumn = @JoinColumn(name = "person_id"),
        inverseJoinColumn = @JoinColumn(name = "course_id"))
    private List<Course> courses = new ArrayList<>();
    
}
