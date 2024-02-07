package testdata.onetoone.bidirectional.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.*;
import testdata.onetoone.unidirectional.House;

@Entity
@Table(name = "addresses")
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne(mappedBy = "address")
    private User user;

//    @OneToOne
//    @JoinColumn(name = "house_id")
//    private House house;
}
