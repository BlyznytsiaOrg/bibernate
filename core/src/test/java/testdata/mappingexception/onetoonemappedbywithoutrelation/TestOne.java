package testdata.mappingexception.onetoonemappedbywithoutrelation;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;

@Entity
public class TestOne {
    @Id
    private Long id;

    @OneToOne(mappedBy = "testOne")
    private TestTwo testTwo;
}
