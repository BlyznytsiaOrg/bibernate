package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class DefaultBibernateSession implements BibernateSession {

    private final Dao dao;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        return dao.findById(entityClass, primaryKey);
    }

    @Override
    public void close() {
        log.info("Close session...");
    }
}
