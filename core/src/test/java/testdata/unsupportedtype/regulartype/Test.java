package testdata.unsupportedtype.regulartype;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import java.util.Random;

@Entity
public class Test {
    @Id
    private Long id;

    private Random random;
}
