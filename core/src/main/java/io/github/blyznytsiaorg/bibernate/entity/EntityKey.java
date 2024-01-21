package io.github.blyznytsiaorg.bibernate.entity;

import java.lang.reflect.Type;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public record EntityKey<T>(Class<T> clazz, Object id, Type keyType) {
}
