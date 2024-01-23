package testdata.simplerespository;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class PersonCustomQueryRepositoryImpl implements PersonCustomQueryRepository {

    private final BibernateSessionFactory sessionFactory;

    @Override
    public List<Person> findMyCustomQuery() {
        try (var bringSession = sessionFactory.openSession()) {
            return List.of(bringSession.findById(Person.class, 1L).orElseThrow());
        }
    }
}
