package testdata.mappingexception.onetooneoncolumn;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;

@Entity
public class TestOne {
    @Id
    private Long id;

    @OneToOne
    @Column(name = "test_two")
    private TestTwo testTwo;
}
