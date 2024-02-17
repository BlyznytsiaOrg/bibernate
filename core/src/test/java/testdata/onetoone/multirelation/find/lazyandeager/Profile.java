package testdata.onetoone.multirelation.find.lazyandeager;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue
    @Column(name = "profiles_id")
    private Long id;

    @Column(name = "profiles_nickname")
    private String nickname;
}
