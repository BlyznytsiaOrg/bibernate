package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isSupportedCollection;

@UtilityClass
public class EntityRelationsUtils {

    private final List<Class<? extends Annotation>> entityAnnotations = List.of(OneToOne.class, ManyToOne.class);
    
    private final List<Class<? extends Annotation>> collectionAnnotations = List.of(OneToMany.class);
    
    public boolean isRegularField(Field field) {
        return !isEntityField(field) && !isCollectionField(field);
    }

    public boolean isEntityField(Field field) {
        if (entityAnnotations.stream().anyMatch(field::isAnnotationPresent)) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                throw new BibernateGeneralException(
                        "Field [%s] from [%s] is annotated with annotation %s that is not applicable for Collections."
                                .formatted(field.getName(), field.getDeclaringClass(), entityAnnotations));
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean isCollectionField(Field field) {
        if (collectionAnnotations.stream().anyMatch(field::isAnnotationPresent)) {
            if (isSupportedCollection(field)) {
                return true;
            } else {
                throw new BibernateGeneralException(
                        ("Field [%s] from [%s] is annotated with a collection annotation %s " +
                                "but is not a supported Collection.")
                                .formatted(field.getName(), field.getDeclaringClass(), collectionAnnotations));
            }
        }

        return false;
    }
    
}
