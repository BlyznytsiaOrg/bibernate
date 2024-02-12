package testdata.onetoone.multirelation.find.lazy;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "houses")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class House {

    @Id
    @GeneratedValue
    @Column(name = "houses_id")
    private Long id;

    @Column(name = "houses_name")
    private String name;
}
