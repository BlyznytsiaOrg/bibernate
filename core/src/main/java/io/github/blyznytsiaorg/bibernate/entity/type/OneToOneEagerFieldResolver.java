package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class OneToOneEagerFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.EAGER;
    }

    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        Class<?> type = field.getType();

        try {
            if (isBidirectional(field)) {
                Object obj = field.getType().getDeclaredConstructor().newInstance();
                for (Field declaredField : field.getType().getDeclaredFields()) {
                    if (Objects.nonNull(declaredField.getAnnotation(OneToOne.class))
                        && (field.getAnnotation(OneToOne.class).mappedBy().equals(declaredField.getName())
                            || Objects.equals(field.getName(), declaredField.getAnnotation(OneToOne.class).mappedBy()))) {
                        setField(declaredField, obj, entity);
                    } else {
                        Object object = resultSet.getObject(columnName(declaredField));
                        setField(declaredField, obj, object);
                    }
                }

                return obj;
            } else {
                Object obj = field.getType().getDeclaredConstructor().newInstance();
                for (Field declaredField : field.getType().getDeclaredFields()) {
                    Object object = resultSet.getObject(columnName(declaredField, type));

                    if (Objects.nonNull(declaredField.getAnnotation(OneToOne.class))) {
                        Class<?> currentFieldType = declaredField.getType();
                        Object oneOnOneRelation = entityPersistent.toEntity(resultSet, currentFieldType);
                        setField(declaredField, obj, oneOnOneRelation);
                    } else {
                        setField(declaredField, obj, object);
                    }
                }

                return obj;
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    "Cannot populate type " + field.getType() + "due to message " + exe.getMessage(), exe
            );
        }
    }
}
