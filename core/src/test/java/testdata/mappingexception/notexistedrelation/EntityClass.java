package testdata.mappingexception.notexistedrelation;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;

@Entity
public class EntityClass {
    @Id
    private Long id;

    @ManyToOne
    private NotEntityClass notEntityClass;
}
