package testdata.onetoone.multirelation.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
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
    @GeneratedValue
    private Long id;

    @Column(name = "name")
    private String name;
}
