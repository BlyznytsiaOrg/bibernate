package testdata.multirelation;

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
    private Long houseId;

    @Column
    private String name;
}
