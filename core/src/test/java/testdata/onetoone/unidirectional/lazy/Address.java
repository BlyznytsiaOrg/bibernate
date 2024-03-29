package testdata.onetoone.unidirectional.lazy;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @Column(name = "addresses_id")
    private Long id;

    @Column(name = "addresses_name")
    private String name;
}
