package testdata.multirelation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.GenerationType;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
    
}
