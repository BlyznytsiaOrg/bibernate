package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
public class EntityMapper {
    private final EntityDao entityDao;

    public  <T> T toEntity(ResultSet resultSet, Class<T> entityClass)
            throws ReflectiveOperationException, SQLException {
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (isRegularField(field)) {
                Object obj = field.get(entity);
                var fieldName = columnName(field);
                var value = resultSet.getObject(fieldName, field.getType());

                if (Objects.isNull(obj)) {
                    field.set(entity, value);
                }
            } else if (isEntityField(field)) {
                var joinColumnName = joinColumnName(field);
                var joinColumnValue = resultSet.getObject(joinColumnName);

                Optional<?> joinEntityById = entityDao.findById(field.getType(), joinColumnValue);
                field.set(entity, joinEntityById.get());
            }
        }

        return entity;
    }
}
