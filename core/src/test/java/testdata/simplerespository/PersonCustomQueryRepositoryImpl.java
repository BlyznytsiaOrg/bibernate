package testdata.simplerespository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSessionFactory;

@RequiredArgsConstructor
@Slf4j
public class PersonCustomQueryRepositoryImpl implements PersonCustomQueryRepository {

    @Override
    public List<Person> findMyCustomQuery() {
        try (var bringSession = getBibernateSessionFactory().openSession()) {
            return List.of(bringSession.findById(Person.class, 1L).orElseThrow());
        }
    }
}
