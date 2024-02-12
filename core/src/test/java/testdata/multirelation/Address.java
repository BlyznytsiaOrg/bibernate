package testdata.multirelation;

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
    @GeneratedValue
    @Column
    private Long addressId;

    @Column
    private String address;

    @OneToOne
    @JoinColumn(name = "address_house_id")
    private House house;
}
