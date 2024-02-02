package testdata.generatedvalue.sequence;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.*;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persons")
@ToString
@Setter
@Getter
public class Person {
  @Id
  @GeneratedValue(strategy = SEQUENCE)
  @Column(name = "id")
  private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;
}
