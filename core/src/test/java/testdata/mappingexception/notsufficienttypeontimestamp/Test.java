package testdata.mappingexception.notsufficienttypeontimestamp;

import io.github.blyznytsiaorg.bibernate.annotation.CreationTimestamp;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

@Entity
public class Test {
    @Id
    private Long id;

    @CreationTimestamp
    private String time;
}
