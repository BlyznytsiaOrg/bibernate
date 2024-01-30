package testdata.simplerespository;

import io.github.blyznytsiaorg.bibernate.annotation.Param;
import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BibernateRepository<User, Long> {

    List<User> findByEnabled(@Param("enable") boolean enabled);

    List<User> findByAgeLessThan(@Param("age") int age);

    List<User> findByAgeLessThanEqual(@Param("age") int age);

    List<User> findByAgeGreaterThan(@Param("age") int age);

    List<User> findByAgeGreaterThanEqual(@Param("age") int age);

    List<User> findByUsernameNotNull();

    List<User> findByUsernameNull();

    Optional<User> findByUsernameAndAge(@Param("username") String username, @Param("age") int age);

    @Query(value = "select count(*) from users group by username having count(username) > ?", nativeQuery = true)
    int countUserDuplicate(@Param("count") int count);
}
