package testdata.mappingexception.foreignkeyconstraintduplicate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.ForeignKey;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;

@Entity
public class TestOne {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name ="testTwo_id", foreignKey = @ForeignKey(name = "test"))
    private TestTwo testTwo;

    @ManyToOne
    @JoinColumn(name ="testThree_id", foreignKey = @ForeignKey(name = "test"))
    private TestThree testThree;
}
