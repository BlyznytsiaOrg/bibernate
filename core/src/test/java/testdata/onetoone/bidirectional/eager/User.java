package testdata.onetoone.bidirectional.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.*;

@Entity
@Table(name = "users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "users_id")
    private Long id;

    @Column(name = "users_first_name")
    private String firstName;

    @Column(name = "users_last_name")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "users_address_id")
    private Address address;
}
