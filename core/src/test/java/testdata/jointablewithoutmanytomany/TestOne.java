package testdata.jointablewithoutmanytomany;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.JoinTable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TestOne {
    @Id
    private Long id;

    @JoinTable(name = "test1_test2", joinColumn = @JoinColumn(name = "test1_id"),
            inverseJoinColumn = @JoinColumn(name = "test1"))
    List<TestTwo> testTwos = new ArrayList<>();
}
