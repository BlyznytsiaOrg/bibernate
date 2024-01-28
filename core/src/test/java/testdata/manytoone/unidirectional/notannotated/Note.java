package testdata.manytoone.unidirectional.notannotated;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;
import testdata.manytoone.unidirectional.Person;

@Getter
@Entity
@Table(name = "notes")
public class Note {
    
    @Id
    private Long id;
    
    private String text;
    
    @JoinColumn(name = "person_id")
    private Person person;
    
}
