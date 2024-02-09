package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;
import io.github.blyznytsiaorg.bibernate.utils.ProxyUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

@Slf4j
public class EntityFieldResolver implements TypeFieldResolver {

    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    @SneakyThrows
    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Class<?> entityClass) {
        var session = BibernateSessionContextHolder.getBibernateSession();

//        if (isBidirectionalOwnerSide(field)) {
//            var columnIdName = columnIdName(field.getDeclaringClass());
//            var idValue = getValueFromResultSetByColumn(resultSet, columnIdName);
//            return session.findAllById(field.getType(), mappedByEntityJoinColumnName(field), idValue)
//                    .get(0);
//        }

        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.EAGER) {
            Class<?> type = field.getType();
            Object o = field.getType().getDeclaredConstructor().newInstance();
            for (Field declaredField : field.getType().getDeclaredFields()) {
                Object object = resultSet.getObject(table(type).concat("_").concat(columnName(declaredField)));
                setField(declaredField, o, object);
            }

            return o;
        }

        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.LAZY) {
            var joinColumnName = joinColumnName(field);
            var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
            Class<?> type = field.getType();
            Supplier<Object> entitySupplier = () -> {
                log.info("Processing LAZY loading of Entity {}", type);
                return session.findById(type, joinColumnValue).get();
            };

            return ProxyUtils.createProxy(type, entitySupplier);
        }

        return null;
    }
}
