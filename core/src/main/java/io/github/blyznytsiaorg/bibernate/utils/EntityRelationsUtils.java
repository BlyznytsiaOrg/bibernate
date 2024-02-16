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

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 * Utility class for handling entity relationships in Bibernate.
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

    /**
     * Checks if a field is a regular field (neither an entity nor a collection).
     *
     * @param field The field to check.
     * @return {@code true} if the field is a regular field, {@code false} otherwise.
     */
    public boolean isRegularField(Field field) {
        return !isEntityField(field) && !isCollectionField(field);
    }

    /**
     * Checks if a field is an entity field.
     *
     * @param field The field to check.
     * @return {@code true} if the field is an entity field, {@code false} otherwise.
     * @throws BibernateGeneralException If the field is annotated with an entity annotation but is a collection.
     */
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

    /**
     * Checks if a field is a collection field.
     *
     * @param field The field to check.
     * @return {@code true} if the field is a collection field, {@code false} otherwise.
     * @throws BibernateGeneralException If the field is annotated with a collection annotation but is not a supported Collection.
     */
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

    /**
     * Checks if a field is an inverse side in a ManyToMany relationship.
     *
     * @param field The field to check.
     * @return {@code true} if the field is an inverse side, {@code false} otherwise.
     */
    public static boolean isInverseSide(Field field) {
        return isManyToMany(field) && !field.isAnnotationPresent(JoinTable.class);
    }

    /**
     * Gets the owning field from an inverse field in a ManyToMany relationship.
     *
     * @param field The inverse field.
     * @return The owning field.
     * @throws BibernateGeneralException If the owning field cannot be obtained.
     */
    public static Field owningFieldByInverse(Field field) {
        try {
            var mappedBy = Optional.ofNullable(field.getAnnotation(ManyToMany.class))
                    .map(ManyToMany::mappedBy)
                    .filter(Predicate.not(String::isEmpty))
                    .orElseThrow(() -> new BibernateGeneralException(UNABLE_TO_GET_OWNING_FIELD_FROM_INVERSE_FIELD
                            .formatted(field.getName())));
            var type = getCollectionGenericType(field);

            return type.getDeclaredField(mappedBy);
        } catch (NoSuchFieldException e) {
            throw new BibernateGeneralException(UNABLE_TO_GET_MAPPED_BY_FIELD_IN_OWNING_ENTITY, e);
        }
    }

    /**
     * Gets the join column name based on the 'mappedBy' attribute of the OneToMany annotation.
     *
     * @param field The field annotated with OneToMany.
     * @return The join column name.
     */
    public static String mappedByJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(OneToMany.class))
          .map(OneToMany::mappedBy)
          .filter(Predicate.not(String::isEmpty))
          .flatMap(mappedByName -> {
              var collectionGenericType = getCollectionGenericType(field);

              return getMappedByColumnName(mappedByName, collectionGenericType);
          })
          .orElse(joinColumnName(field));
    }

    /**
     * Retrieves the name of the column associated with a mapped-by field in a collection.
     *
     * @param mappedByName           The name of the field that is mapped by another field.
     * @param collectionGenericType  The type of the collection.
     * @return                      An Optional containing the name of the column associated with the mapped-by field,
     *                              or an empty Optional if no matching field is found.
     */
    private static Optional<String> getMappedByColumnName(String mappedByName, Class<?> collectionGenericType) {
        return Arrays.stream(collectionGenericType.getDeclaredFields())
          .filter(f -> Objects.equals(f.getName(), mappedByName))
          .findFirst()
          .map(EntityReflectionUtils::joinColumnName);
    }

    /**
     * Retrieves the names of bidirectional relations for a given many-to-many association field of an entity.
     *
     * @param entityClass   The class of the entity.
     * @param field         The field representing the many-to-many association.
     * @return              A list of names of bidirectional relations for the given field.
     */
    public static List<String> bidirectionalRelations(Class<?> entityClass, Field field) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(EntityRelationsUtils::isManyToMany)
                .filter(f -> f.getAnnotation(ManyToMany.class).mappedBy().equals(field.getName())
                        || f.getName().equals(field.getAnnotation(ManyToMany.class).mappedBy()))
                .map(Field::getName)
                .toList();
    }

    /**
     * Retrieves the CascadeType values specified in the cascade attribute of a given annotation.
     *
     * @param annotation The annotation from which to retrieve cascade types.
     * @return A list of CascadeType values specified in the cascade attribute of the annotation,
     *         or an empty list if the cascade attribute is not present or cannot be accessed.
     */
    public static List<CascadeType> getCascadeTypesFromAnnotation(Annotation annotation) {
        try {
            var cascadeMethod = annotation.annotationType().getDeclaredMethod("cascade");
            var cascadeTypes = (CascadeType[]) cascadeMethod.invoke(annotation);
            return Arrays.asList(cascadeTypes);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if a field is annotated with @OneToMany.
     *
     * @param field The field to check.
     * @return True if the field is annotated with @OneToMany, false otherwise.
     */
    public boolean isOneToMany(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    /**
     * Checks if a field is annotated with @ManyToMany.
     *
     * @param field The field to check.
     * @return True if the field is annotated with @ManyToMany, false otherwise.
     */
    public boolean isManyToMany(Field field) {
        return field.isAnnotationPresent(ManyToMany.class);
    }
}
