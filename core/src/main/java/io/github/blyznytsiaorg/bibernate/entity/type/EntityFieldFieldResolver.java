package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

//@RequiredArgsConstructor
public class EntityFieldFieldResolver implements TypeFieldResolver {

//    private final BibernateSession session;

    @Override
    public boolean isResolved(Field field) {
        return EntityReflectionUtils.isEntityField(field);
    }

    @Override
    public Object setValueToField(Field field, ResultSet resultSet) throws SQLException {
        var session = BibernateSessionContextHolder.getBibernateSession();
        var joinColumnName = joinColumnName(field);
        var joinColumnValue = resultSet.getObject(joinColumnName);

        //TODO: check what hibernate do if entity is not found
        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
