package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.NONE;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

/**
 * Implementation of the insert generation without ID generation in Bibernate.
 * <p>
 * This generator is responsible for handling entities with their own IDs already set.
 *
 * <p>This generator is suitable for entities where the IDs are provided, and no generation is needed.
 * The generator operates by inserting entities into the database without attempting to generate IDs during the process.</p>
 *
 * <p>This class is designed to be used when dealing with entities that have their own unique identifiers specified,
 * and there is no need for the database to generate new IDs during the insertion process.</p>
 *
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class NoneIdGenerator extends AbstractGenerator implements Generator {

    /**
     * Constructs an NoneIdGenerator with the specified Bibernate database settings
     * and a list to store executed queries.
     *
     * @param bibernateDatabaseSettings The database settings for Bibernate.
     * @param executedQueries           The list to store executed queries.
     */
    public NoneIdGenerator(
            BibernateDatabaseSettings bibernateDatabaseSettings,
            List<String> executedQueries) {
        super(bibernateDatabaseSettings, executedQueries);
    }

    /**
     * Returns NONE GenerationType for further understanding of what type
     * of generator we are dealing with
     *
     */
    @Override
    public GenerationType type() {
        return NONE;
    }

    /**
     * Handles the generation of insert query using id from the entity.
     * This method constructs the INSERT query, prepares the statement, populates it with entity values,
     * and executes the batch for improved performance. It also logs executed queries and handles many-to-many join tables.
     *
     * @param entityClass The class of the entities being handled.
     * @param entities    The collection of entities for which primary keys are generated.
     * @param dataSource  The data source for obtaining a database connection.
     * @param <T>         The type of entities in the collection.
     */
    @Override
    public <T> void handle(Class<T> entityClass, Collection<T> entities, DataSource dataSource) {
        var tableName = table(entityClass);
        var query = insert(entityClass, tableName);
        var entityArr = entities.toArray();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);
            for (int i = 0; i < entityArr.length; i++) {
                populatePreparedStatement(entityArr[i], ps);
                ps.addBatch();
                addToExecutedQueries(query);

                if (i % getBatchSize() == 0 && i != 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            showSql(() -> log.debug(QUERY, query));
            insertManyToManyJoinTable(entities, dataSource);
        } catch (Exception e) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entityClass, e.getMessage()), e);
        } finally {
            close(connection, ps);
        }
    }
}
