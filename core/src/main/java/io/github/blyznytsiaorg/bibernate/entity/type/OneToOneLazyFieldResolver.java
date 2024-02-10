package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.ProxyUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class OneToOneLazyFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.LAZY;
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Class<?> entityClass) {
        var session = BibernateContextHolder.getBibernateSession();

        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
        Class<?> type = field.getType();
        Supplier<?> entitySupplier = () -> session.findById(type, joinColumnValue).orElse(null);

        return ProxyUtils.createProxy(type, entitySupplier);
    }
}
