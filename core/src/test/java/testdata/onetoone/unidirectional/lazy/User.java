package testdata.onetoone.unidirectional.lazy;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import lombok.*;

@Entity
@Table(name = "users")
@ToString
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_address_id")
    private Address address;
}
