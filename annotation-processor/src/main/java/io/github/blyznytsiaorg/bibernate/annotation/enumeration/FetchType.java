package io.github.blyznytsiaorg.bibernate.annotation.enumeration;

/**
 * Enumeration representing the fetching strategy used to load related entities in a relational mapping.
 * <p>
 * This enumeration specifies two fetching strategies:
 * <ul>
 *     <li>{@link FetchType#EAGER}: Indicates that related entities should be loaded eagerly, i.e., at the same
 *     time as the owning entity.</li>
 *     <li>{@link FetchType#LAZY}: Indicates that related entities should be loaded lazily, i.e., only when
 *     they are explicitly accessed or requested.</li>
 * </ul>
 */
public enum FetchType {
    /**
     * Indicates that related entities should be loaded eagerly, i.e., at the same time as the owning entity.
     */
    EAGER,
    /**
     * Indicates that related entities should be loaded lazily, i.e., only when they are explicitly accessed or requested.
     */
    LAZY
}
