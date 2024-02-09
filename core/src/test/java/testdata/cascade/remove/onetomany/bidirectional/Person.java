package testdata.cascade.remove.onetomany.bidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.*;
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

    @OneToMany(mappedBy = "person", cascade = CascadeType.REMOVE)
    private List<Note> notes = new ArrayList<>();

}
