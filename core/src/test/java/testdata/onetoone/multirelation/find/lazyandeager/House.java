package testdata.onetoone.multirelation.find.lazyandeager;

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
    @Column(name = "houses_id")
    private Long id;

    @Column(name = "houses_name")
    private String name;
}
