package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.transaction.TransactionalDatasource;
import lombok.extern.slf4j.Slf4j;

/**
 * The StatelessSession class represents a stateless session for executing database operations.
 * It utilizes a transactional datasource obtained from Bibernate database settings.
 *
 * @see TransactionalDatasource
 * @see BibernateDatabaseSettings
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class StatelessSession {

    /**
     * The transactional datasource used for database operations.
     */
    private final TransactionalDatasource dataSource;

    /**
     * Constructs a new StatelessSession with the provided Bibernate database settings.
     *
     * @param bibernateDatabaseSettings the Bibernate database settings
     */
    public StatelessSession(BibernateDatabaseSettings bibernateDatabaseSettings) {
        dataSource = bibernateDatabaseSettings.getDataSource();
    }

    /**
     * Retrieves the Transactional datasource used by this stateless session.
     *
     * @return the transactional datasource
     */
    public TransactionalDatasource getDataSource() {
       return dataSource;
    }

    /**
     * Closes the stateless session by closing the associated datasource.
     */
    public void close() {
        if (dataSource != null) {
            log.trace("Close dataSource...");
            dataSource.close();
        }
    }
}
