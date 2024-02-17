package testdata.onetoone.multirelation.find.lazyandeager;

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
    @GeneratedValue
    @Column(name = "addresses_id")
    private Long id;

    @Column(name = "addresses_name")
    private String name;

    @OneToOne
    @JoinColumn(name = "addresses_house_id")
    private House house;
}
