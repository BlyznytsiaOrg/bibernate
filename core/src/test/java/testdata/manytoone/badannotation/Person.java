package testdata.manytoone.badannotation;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "persons")
public class Person {
    
    @Id
    private Long id;
    
    private String firstName;
    
    private String lastName;
    
}
