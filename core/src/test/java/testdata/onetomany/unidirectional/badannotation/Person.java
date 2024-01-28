package testdata.onetomany.unidirectional.badannotation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
    @OneToMany
    @JoinColumn(name = "person_id") // This column is in the Notes table
    private Note note;
    
}
