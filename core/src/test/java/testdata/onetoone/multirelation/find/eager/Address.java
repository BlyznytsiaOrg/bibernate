package testdata.onetoone.multirelation.find.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
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

    @OneToOne
    @JoinColumn(name = "addresses_house_id")
    private House house;
}
