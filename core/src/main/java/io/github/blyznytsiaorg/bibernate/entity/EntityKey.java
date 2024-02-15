package io.github.blyznytsiaorg.bibernate.entity;

import java.lang.reflect.Type;

/**
 * Represents the key of an entity, consisting of the entity class, ID, and key type.
 * This record encapsulates the necessary information to uniquely identify an entity.
 *
 * @param <T> The type of the entity.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public record EntityKey<T>(Class<T> clazz, Object id, Type keyType) {
}
