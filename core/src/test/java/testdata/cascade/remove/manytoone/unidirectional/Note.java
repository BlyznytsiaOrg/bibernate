package testdata.cascade.remove.manytoone.unidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    private Long id;

    private String text;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "person_id")
    private Person person;

}
