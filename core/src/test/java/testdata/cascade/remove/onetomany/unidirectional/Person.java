package testdata.cascade.remove.onetomany.unidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "person_id")
    private List<Note> notes = new ArrayList<>();

}
