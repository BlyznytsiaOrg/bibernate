package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.ProxyUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSetByColumn;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

public class LazyEntityFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return Optional.ofNullable(field.getAnnotation(ManyToOne.class))
                .map(annotation -> annotation.fetch() == FetchType.LAZY)
                .orElse(Optional.ofNullable(field.getAnnotation(OneToOne.class))
                        .map(annotation -> annotation.fetch() == FetchType.LAZY)
                        .orElse(false));
    }

    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        var session = BibernateContextHolder.getBibernateSession();

        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
        Class<?> type = field.getType();
        Supplier<?> entitySupplier = () -> session.findById(type, joinColumnValue).orElse(null);

        return ProxyUtils.createProxy(type, entitySupplier);
    }
}
