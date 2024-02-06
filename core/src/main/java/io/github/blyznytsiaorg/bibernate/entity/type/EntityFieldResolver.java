package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.collection.PersistentEntityHandler;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class EntityFieldResolver implements TypeFieldResolver {

    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    @SneakyThrows
    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        var session = BibernateSessionContextHolder.getBibernateSession();

        if (isBidirectionalOwnerSide(field)) {
            var columnIdName = columnIdName(field.getDeclaringClass());
            var idValue = getValueFromResultSetByColumn(resultSet, columnIdName);
            return session.findAllById(field.getType(), mappedByEntityJoinColumnName(field), idValue)
                    .get(0);
        }

        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.EAGER) {
            Class<?> type = field.getType();
            Supplier<Object> supplier = () -> {
                try {
                    Object o = field.getType().getDeclaredConstructor().newInstance();
                    for (Field declaredField : field.getType().getDeclaredFields()) {
                        Object object = resultSet.getObject(table(type).concat("_").concat(columnName(declaredField)));
                        setField(declaredField, o, object);
                    }

                    return o;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            return supplier;
        }

//        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.LAZY) {
//            var joinColumnName = joinColumnName(field);
//            var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
//            Class<?> type = field.getType();
//            Supplier<?> entitySupplier = () -> session.findById(type, joinColumnValue);
//
////            return  Proxy.newProxyInstance(
////                    this.getClass().getClassLoader(),
////                    new Class<?>[]{entitySupplier},
////                    new PersistentEntityHandler<>(entitySupplier)
//            );
//        }

        return null;
    }
}
