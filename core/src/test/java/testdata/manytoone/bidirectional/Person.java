package testdata.manytoone.bidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "persons")
public class Person {

    @Id
    private Long id;

    private String firstName;

    private String lastName;
    
    @OneToMany(mappedBy = "person")
    private List<Note> notes;

}
