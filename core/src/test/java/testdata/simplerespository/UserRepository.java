package testdata.simplerespository;

import io.github.blyznytsiaorg.bibernate.annotation.Param;
import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;

import java.util.List;

public interface UserRepository extends BibernateRepository<User, Long> {

    List<User> findByEnabled(@Param("enable") boolean enabled);

    List<User> findByAgeLessthan(@Param("age") int age);

    List<User> findByAgeLessthanequal(@Param("age") int age);

    List<User> findByAgeGreaterthan(@Param("age") int age);

    List<User> findByAgeGreaterthanequal(@Param("age") int age);

    List<User> findByUsernameNotnull();

    List<User> findByUsernameNull();
}
