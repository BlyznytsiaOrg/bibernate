package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class EntityRelationsUtils {

    private static final String FIELD_WITH_ANNOTATION_NOT_APPLICABLE_FOR_COLLECTIONS =
            "Field [%s] from [%s] is annotated with annotation %s that is not applicable for Collections.";
    private static final String FIELD_WITH_ANNOTATION_APPLICABLE_ONLY_FOR_COLLECTIONS = 
            "Field [%s] from [%s] is annotated with a collection annotation %s but is not a supported Collection.";
    private static final String UNABLE_TO_GET_OWNING_FIELD_FROM_INVERSE_FIELD =
            "Unable to get owning field from inverse field [%s] without @ManyToMany(mappedBy)";
    public static final String UNABLE_TO_GET_MAPPED_BY_FIELD_IN_OWNING_ENTITY = 
            "Unable to get mappedBy field in owning Entity";
    
    private final List<Class<? extends Annotation>> entityAnnotations = List.of(OneToOne.class, ManyToOne.class);
    private final List<Class<? extends Annotation>> collectionAnnotations = List.of(OneToMany.class, ManyToMany.class);

    public boolean isRegularField(Field field) {
        return !isEntityField(field) && !isCollectionField(field);
    }

    public boolean isEntityField(Field field) {
        if (entityAnnotations.stream().anyMatch(field::isAnnotationPresent)) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                throw new BibernateGeneralException(FIELD_WITH_ANNOTATION_NOT_APPLICABLE_FOR_COLLECTIONS
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
                throw new BibernateGeneralException(FIELD_WITH_ANNOTATION_APPLICABLE_ONLY_FOR_COLLECTIONS
                        .formatted(field.getName(), field.getDeclaringClass(), collectionAnnotations));
            }
        }

        return false;
    }

    public static boolean isInverseSide(Field field) {
        return isManyToMany(field) && !field.isAnnotationPresent(JoinTable.class);
    }

    public static Field owningFieldByInverse(Field field) {
        try {
            String mappedBy = Optional.ofNullable(field.getAnnotation(ManyToMany.class))
                    .map(ManyToMany::mappedBy)
                    .filter(Predicate.not(String::isEmpty))
                    .orElseThrow(() -> new BibernateGeneralException(UNABLE_TO_GET_OWNING_FIELD_FROM_INVERSE_FIELD
                            .formatted(field.getName())));
            Class<?> type = getCollectionGenericType(field);

            return type.getDeclaredField(mappedBy);
        } catch (NoSuchFieldException e) {
            throw new BibernateGeneralException(UNABLE_TO_GET_MAPPED_BY_FIELD_IN_OWNING_ENTITY, e);
        }
    }

    public static String mappedByJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(OneToMany.class))
          .map(OneToMany::mappedBy)
          .filter(Predicate.not(String::isEmpty))
          .flatMap(mappedByName -> {
              Class<?> collectionGenericType = getCollectionGenericType(field);

              return getMappedByColumnName(mappedByName, collectionGenericType);
          })
          .orElse(joinColumnName(field));
    }

    private static Optional<String> getMappedByColumnName(String mappedByName, Class<?> collectionGenericType) {
        return Arrays.stream(collectionGenericType.getDeclaredFields())
          .filter(f -> Objects.equals(f.getName(), mappedByName))
          .findFirst()
          .map(EntityReflectionUtils::joinColumnName);
    }

    public static List<String> bidirectionalRelations(Class<?> entityClass, Field field) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(EntityRelationsUtils::isManyToMany)
                .filter(f -> f.getAnnotation(ManyToMany.class).mappedBy().equals(field.getName())
                        || f.getName().equals(field.getAnnotation(ManyToMany.class).mappedBy()))
                .map(Field::getName)
                .toList();
    }

    public static List<CascadeType> getCascadeTypesFromAnnotation(Annotation annotation) {
        try {
            Method cascadeMethod = annotation.annotationType().getDeclaredMethod("cascade");
            CascadeType[] cascadeTypes = (CascadeType[]) cascadeMethod.invoke(annotation);
            return Arrays.asList(cascadeTypes);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isOneToMany(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    public boolean isManyToMany(Field field) {
        return field.isAnnotationPresent(ManyToMany.class);
    }

    public boolean hasOneToOneRelation(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToOne.class));
    }

}
