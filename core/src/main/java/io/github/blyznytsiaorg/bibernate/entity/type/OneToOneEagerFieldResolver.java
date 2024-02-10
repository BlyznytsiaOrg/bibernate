package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class OneToOneEagerFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.EAGER;
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Class<?> entityClass) {
        Class<?> type = field.getType();
        try {
            Object obj = field.getType().getDeclaredConstructor().newInstance();
            for (Field declaredField : field.getType().getDeclaredFields()) {
                Object object = resultSet.getObject(table(type).concat("_").concat(columnName(declaredField)));
                setField(declaredField, obj, object);
            }

            return obj;
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    "Cannot populate type " + field.getType() + "due to message " + exe.getMessage(), exe
            );
        }
    }
}
