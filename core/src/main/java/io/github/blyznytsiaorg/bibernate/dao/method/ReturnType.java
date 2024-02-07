package io.github.blyznytsiaorg.bibernate.dao.method;

import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
/**
 * Represents the return type of a method, including the entity class and generic parameters if applicable.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@ToString
@Getter
public class ReturnType {

    /**
     * The parameterized type representing the generic entity class.
     */
    private ParameterizedType genericEntityClass;

    /**
     * The raw class representing the entity class.
     */
    private Class<?> entityClass;

    /**
     * Constructs a ReturnType instance with a parameterized entity class.
     *
     * @param entityClass The parameterized type representing the generic entity class.
     */
    public ReturnType(ParameterizedType entityClass) {
        this.genericEntityClass = entityClass;
    }

    /**
     * Constructs a ReturnType instance with a raw entity class.
     *
     * @param entityClass The raw class representing the entity class.
     */
    public ReturnType(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
}
