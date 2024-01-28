package testdata.onetomany.unidirectional.notannotated;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
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

    @JoinColumn(name = "person_id") // This column is in the Notes table
    private List<Note> notes = new ArrayList<>();
    
}
