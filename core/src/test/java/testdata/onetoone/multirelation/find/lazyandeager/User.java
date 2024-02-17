package testdata.onetoone.multirelation.find.lazyandeager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import lombok.*;

@Entity
@Table(name = "users")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    @Column(name = "users_id")
    private Long id;

    @Column(name = "users_first_name")
    private String firstName;

    @Column(name = "users_last_name")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "users_address_id")
    private Address address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_profile_id")
    private Profile profile;
}
