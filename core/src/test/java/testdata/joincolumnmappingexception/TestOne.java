package testdata.joincolumnmappingexception;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;

@Entity
public class TestOne {

    @Id
    private Long id;

    @JoinColumn(name = "test_two_id")
    private TestTwo testTwo;
}
