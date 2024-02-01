package testdata.onetomany.bidirectional;

import java.util.ArrayList;
import java.util.List;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "persons")
public class Person {

    @Id
    private Long id;

    private String firstName;

    private String lastName;
    
    @OneToMany(mappedBy = "person")
    private List<Note> notes = new ArrayList<>();

}
