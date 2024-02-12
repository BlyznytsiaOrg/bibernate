package io.github.blyznytsiaorg.bibernate.ddl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class DDLProcessor {
    private final DDLQueryCreator ddlQueryCreator;
    private final HikariDataSource dataSource;

    public DDLProcessor(DDLQueryCreator ddlQueryCreator, HikariDataSource dataSource) {
        this.ddlQueryCreator = ddlQueryCreator;
        this.dataSource = dataSource;
    }

    public void processCreateProperty() {
        ddlQueryCreator.getDdlMetadata()
                .values()
                .forEach(this::executeQuery);
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
}
