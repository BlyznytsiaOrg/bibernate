package testdata.onetomany.bidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
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
