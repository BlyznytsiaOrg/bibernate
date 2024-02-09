package io.github.blyznytsiaorg.bibernate.transaction;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setIdField;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Transaction {

    private final Connection connection;

    private final Set<Object> updatedEntities = new HashSet<>();

    public void start() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection.commit();
        connection.close();
        updatedEntities.clear();
    }

    public void rollback() throws SQLException {
        connection.rollback();
        connection.close();
        rollbackAllIds();
    }

    public void addUpdatedEntity(Object entity) {
        updatedEntities.add(entity);
    }

    private void rollbackAllIds() {
        for (var entity : updatedEntities) {
            setIdField(entity, null);
        }
    }
}
