package testdata.generatedvalue.sequencegenerator;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.SequenceGenerator;
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
  @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_GENERATOR_PERSON")
  @SequenceGenerator(name = "SEQ_GENERATOR_PERSON", sequenceName = "person_id_custom_seq", initialValue = 5, allocationSize = 5)
  @Column(name = "id")
  private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;
}
