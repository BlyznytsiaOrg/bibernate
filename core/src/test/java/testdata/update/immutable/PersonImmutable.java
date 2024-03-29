package testdata.update.immutable;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persons")
@ToString
@Setter
@Getter
@Immutable
public class PersonImmutable {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
}
