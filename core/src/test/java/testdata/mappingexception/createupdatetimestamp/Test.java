package testdata.mappingexception.createupdatetimestamp;

import io.github.blyznytsiaorg.bibernate.annotation.CreationTimestamp;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
public class Test {
    @Id
    private Long id;

    @CreationTimestamp
    @UpdateTimestamp
    private LocalDateTime createdAt;
}
