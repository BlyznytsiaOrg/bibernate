package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_GET_ID_FROM_SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class SequenceIdGenerator extends AbstractGenerator implements Generator {
    private static final String SELECT_NEXT_QUERY = "select nextval('%s');";

    private final Map<Class<?>, SequenceConf> sequences = new HashMap<>();

    public SequenceIdGenerator(
            BibernateDatabaseSettings bibernateDatabaseSettings,
            List<String> executedQueries) {
        super(bibernateDatabaseSettings, executedQueries);
    }

    @Override
    public GenerationType type() {
        return SEQUENCE;
    }

    @Override
    public <T> void handle(Class<T> entityClass, Collection<T> entities, DataSource dataSource) {
        var tableName = table(entityClass);
        var query = insert(entityClass, tableName);
        var entityArr = entities.toArray();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            for (int i = 0; i < entityArr.length; i++) {
                var generatedId = generateId(entityClass, tableName, dataSource);
                populatePreparedStatement(entityArr[i], statement, generatedId);
                statement.addBatch();
                setIdField(entityArr[i], generatedId);
                addToExecutedQueries(query);

                if (i % getBatchSize() == 0 && i != 0) {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();

            showSql(() -> log.debug(QUERY, query));
        } catch (Exception e) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entityClass, e.getMessage()), e);
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

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {
            var rs = statement.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
            log.debug("Next ID:[{}] was fetched from db for sequence:[{}]", result, sequenceName);
        } catch (Exception e) {
            throw new BibernateGeneralException(CANNOT_GET_ID_FROM_SEQUENCE.formatted(sequenceName), e);
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
