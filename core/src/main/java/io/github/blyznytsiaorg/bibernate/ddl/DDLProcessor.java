package io.github.blyznytsiaorg.bibernate.ddl;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;
import java.sql.SQLException;
import java.util.List;

/**
 * The DDLProcessor class is responsible for processing Data Definition Language (DDL) queries
 * to create database schema elements.
 *
 * @see DDLQueryCreator
 * @see BibernateDataSource
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class DDLProcessor {
    private final DDLQueryCreator ddlQueryCreator;
    private final BibernateDataSource dataSource;

    public DDLProcessor(DDLQueryCreator ddlQueryCreator, BibernateDataSource dataSource) {
        this.ddlQueryCreator = ddlQueryCreator;
        this.dataSource = dataSource;
    }

    /**
     * Processes the creation of database properties by executing the generated DDL queries.
     * <p>
     * Retrieves the DDL metadata from the DDLQueryCreator and executes each set of DDL queries
     * using the DataSource. Queries are executed as batches for optimization and atomicity.
     */
    public void processCreateProperty() {
        ddlQueryCreator.getDdlMetadata()
                .values()
                .forEach(this::executeQuery);
    }

    /**
     * Executes a batch of SQL queries against the database using the provided connection.
     * <p>
     * This method adds each query to a batch using the Statement object obtained from the connection,
     * then executes the batch of queries.
     *
     * @param queries the list of SQL queries to execute as a batch
     * @throws BibernateGeneralException if an SQL exception occurs while executing the batch operation
     */
    private void executeQuery(List<String> queries) {
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                for (String query : queries) {
                    statement.addBatch(query);
                    log.debug("Bibernate: {}", query);
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            throw new BibernateGeneralException("Can't execute batch operation", e);
        }
    }
}
