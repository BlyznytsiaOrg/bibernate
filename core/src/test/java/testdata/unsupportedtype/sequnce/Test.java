package testdata.unsupportedtype.sequnce;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import java.time.LocalDateTime;

@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private LocalDateTime id;
}
