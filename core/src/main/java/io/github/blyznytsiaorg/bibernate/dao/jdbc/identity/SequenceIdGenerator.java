package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.sql.Connection;
import java.sql.PreparedStatement;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_GET_ID_FROM_SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

/**
 * Implementation of the sequence-based ID generator in Bibernate.
 * <p>
 * This generator is responsible for handling entities with sequence-based
 * generation of primary key values.
 * This SequenceIdGenerator generator anticipates that the identifier will be
 * generated in additional query to database before an INSERT operation into the table. </p>
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class SequenceIdGenerator extends AbstractGenerator implements Generator {
    private static final String SELECT_NEXT_QUERY = "select nextval('%s');";

    private final Map<Class<?>, SequenceConf> sequences = new HashMap<>();

    /**
     * Constructs an SequenceIdGenerator with the specified Bibernate database settings
     * and a list to store executed queries.
     *
     * @param bibernateDatabaseSettings The database settings for Bibernate.
     * @param executedQueries           The list to store executed queries.
     */
    public SequenceIdGenerator(
            BibernateDatabaseSettings bibernateDatabaseSettings,
            List<String> executedQueries) {
        super(bibernateDatabaseSettings, executedQueries);
    }

    /**
     * Returns SEQUENCE GenerationType for further understanding of what type
     * of generator we are dealing with
     *
     */
    @Override
    public GenerationType type() {
        return SEQUENCE;
    }

    /**
     * Handles the generation of sequence-based primary keys for a collection of entities
     * and inserts the entities into the database using batch processing.
     * Gets generated id from the sequence of database and sets it to the entity.
     * If the method will be performed in transaction then id will be cleaned up
     * from the entity in case of rollback. Can overbook set of sequences (depends on
     * allocation size property) and use it to avoid redundant sql queries to database
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
                var generatedId = generateId(entityClass, tableName, dataSource);
                populatePreparedStatement(entityArr[i], ps, generatedId);
                ps.addBatch();
                setIdField(entityArr[i], generatedId);
                addUpdatedEntity(entityArr[i]);
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

    private Object generateId(Class<?> entityClass, String tableName, DataSource dataSource) {
        log.debug("Generating ID for entityClass {}", entityClass);
        var seqConf = getSequenceConf(entityClass, tableName);
        var id = seqConf.getNextId();
        if (id == null) {
            seqConf.setNextPortionOfIds(getNextIdFromDbSeq(dataSource, seqConf.getName()));
            id = seqConf.getNextId();
        }
        return id;
    }

    private Long getNextIdFromDbSeq(DataSource dataSource, String sequenceName) {
        Long result = null;
        var query = SELECT_NEXT_QUERY.formatted(sequenceName);

        addToExecutedQueries(query);
        showSql(() -> log.debug(QUERY, query));

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);
            var rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
            log.debug("Next ID:[{}] was fetched from db for sequence:[{}]", result, sequenceName);
        } catch (Exception e) {
            throw new BibernateGeneralException(CANNOT_GET_ID_FROM_SEQUENCE.formatted(sequenceName), e);
        } finally {
            close(connection, ps);
        }
        return result;
    }

    private SequenceConf getSequenceConf(Class<?> entityClass, String tableName) {
        var seqConf = sequences.get(entityClass);
        if (seqConf == null) {
            seqConf = getGeneratedValueSequenceConfig(entityClass, tableName);
            sequences.put(entityClass, seqConf);
        }
        return seqConf;
    }
}
