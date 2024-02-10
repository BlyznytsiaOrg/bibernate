package testdata.mappingexception.manytomanymappedbywithnorelation;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TestOne {
    @Id
    private Long id;

    @ManyToMany(mappedBy = "testOnes")
    private List<TestTwo> testTwos = new ArrayList<>();
}
