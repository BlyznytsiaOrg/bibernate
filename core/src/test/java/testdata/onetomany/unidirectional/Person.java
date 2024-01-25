package testdata.onetomany.unidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
    private List<Note> notes = new ArrayList<>();
    
}
