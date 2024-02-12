package testdata.unsupportedtype.idgeneration;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

@Entity
public class Test {
    @Id
    @GeneratedValue
    private String id;
}
