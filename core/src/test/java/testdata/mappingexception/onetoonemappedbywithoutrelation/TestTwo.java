package testdata.mappingexception.onetoonemappedbywithoutrelation;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

@Entity
public class TestTwo {
    @Id
    private Long id;
}
