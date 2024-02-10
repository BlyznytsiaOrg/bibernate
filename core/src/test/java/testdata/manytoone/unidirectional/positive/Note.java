package testdata.manytoone.unidirectional.positive;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    private Long id;

    private String text;
    
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

}
