package testdata.onetoone.bidirectional.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.*;

@Entity
@Table(name = "users")
//@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
