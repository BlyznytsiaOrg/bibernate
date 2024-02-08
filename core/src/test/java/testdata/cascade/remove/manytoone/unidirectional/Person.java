package testdata.cascade.remove.manytoone.unidirectional;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
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

}
