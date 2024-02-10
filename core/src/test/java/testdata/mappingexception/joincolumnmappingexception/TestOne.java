package testdata.mappingexception.joincolumnmappingexception;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.IgnoreEntity;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;

@Entity
@IgnoreEntity
public class TestOne {

    @Id
    private Long id;

    @JoinColumn(name = "test_two_id")
    private TestTwo testTwo;
}
