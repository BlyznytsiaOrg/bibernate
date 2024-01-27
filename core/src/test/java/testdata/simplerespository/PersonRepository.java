package testdata.simplerespository;

import io.github.blyznytsiaorg.bibernate.annotation.Param;
import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;

import java.util.List;

public interface PersonRepository extends BibernateRepository<Person, Long>, PersonCustomQueryRepository {
    List<Person> findByFirstNameOrLastName(@Param("first_name") String firstName, @Param("last_name") String lastName);

    List<Person> findByFirstNameEquals(@Param("first_name") String fistName);

    List<Person> findByFirstNameLike(@Param("first_name") String firstNameStart);

    @Query(value = "SELECT p FROM Person p WHERE p.firstName = ?")
    List<Person> findByFirstName(@Param("first_name") String firstName);
}
