package testdata.manytoone.badannotation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;
import testdata.manytoone.unidirectional.positive.Person;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "notes")
public class Note {
    
    @Id
    private Long id;
    
    private String text;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private List<Person> persons = new ArrayList<>();
    
}
