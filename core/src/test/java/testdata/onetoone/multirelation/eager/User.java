package testdata.onetoone.multirelation.eager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
