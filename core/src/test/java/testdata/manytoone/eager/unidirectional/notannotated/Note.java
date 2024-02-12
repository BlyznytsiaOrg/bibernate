package testdata.manytoone.eager.unidirectional.notannotated;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;
import testdata.manytoone.eager.unidirectional.positive.Person;

@Getter
@Entity
@Table(name = "notes")
@IgnoreEntity
public class Note {
    
    @Id
    private Long id;
    
    private String text;
    

    private Person person;
}
