package testdata.onetoone.unidirectional.eager;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.*;

@Entity
@Table(name = "houses")
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class House {
    @Id
    @Column(name = "houses_id")
    private Long id;

    @Column(name = "houses_name")
    private String name;
}
