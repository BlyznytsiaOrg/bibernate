package testdata.mappingexception.foreignkeyconstraintduplicate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

@Entity
public class TestThree {
    @Id
    private Long id;
}
