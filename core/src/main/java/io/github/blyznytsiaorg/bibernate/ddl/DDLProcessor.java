package io.github.blyznytsiaorg.bibernate.ddl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Slf4j
public class DDLProcessor {
    private final DDLQueryCreator ddlQueryCreator;
    private final HikariDataSource dataSource;

    public DDLProcessor(DDLQueryCreator ddlQueryCreator, HikariDataSource dataSource) {
        this.ddlQueryCreator = ddlQueryCreator;
        this.dataSource = dataSource;
    }

    public void processCreateProperty() {
        Queue<List<String>> property = createPropertyQueries();
        while (!property.isEmpty()) {
            List<String> poll = property.poll();
            executeQuery(poll);
        }
    }

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

    private Queue<List<String>> createPropertyQueries() {
        Queue<List<String>> envokeQueries = new ArrayDeque<>();
        List<String> dropConstraints = ddlQueryCreator.getDropConstraints();
        List<String> dropTables = ddlQueryCreator.getDropTables();
        List<String> dropSequences = ddlQueryCreator.getDropSequences();
        List<String> createSequences = ddlQueryCreator.getCreateSequences();
        List<String> createTables = ddlQueryCreator.getCreateTables();
        List<String> createIndex = ddlQueryCreator.getCreateIndex();
        List<String> createConstraints = ddlQueryCreator.getCreateConstraints();
        envokeQueries.add(dropConstraints);
        envokeQueries.add(dropTables);
        envokeQueries.add(dropSequences);
        envokeQueries.add(createSequences);
        envokeQueries.add(createTables);
        envokeQueries.add(createIndex);
        envokeQueries.add(createConstraints);
        return envokeQueries;
    }
}
