package io.github.blyznytsiaorg.bibernate.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class EntityMapper {

    public <T> T toEntity(ResultSet resultSet, Class<T> entityClass)
            throws ReflectiveOperationException, SQLException {
        
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            var fieldName = columnName(field);
            field.setAccessible(true);
            Object obj = field.get(entity);

            if (Objects.isNull(obj)) {
                var value = resultSet.getObject(fieldName, field.getType());
                field.set(entity, value);
            }
        }

        return entity;
    }
}
