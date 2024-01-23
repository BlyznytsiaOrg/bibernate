package testdata.simplerespository;

import io.github.blyznytsiaorg.bibernate.annotation.Param;
import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;

import java.util.List;

public interface PersonRepository extends BibernateRepository<Person, Long>, PersonCustomQueryRepository {
    List<Person> findByFirstNameOrLastName(@Param("first_name") String firstName, @Param("last_name") String lastName);

    List<Person> findByFirstNameEquals(@Param("first_name") String fistName);

    List<Person> findByFirstNameLike(@Param("first_name") String firstNameStart);
}
