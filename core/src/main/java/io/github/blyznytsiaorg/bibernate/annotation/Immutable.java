package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated entity is immutable, meaning its state cannot be changed after creation.
 *
 * Immutable objects are thread-safe and have several benefits, including simplified concurrency control
 * and easier reasoning about their behavior. They are particularly useful in concurrent and distributed
 * systems where shared mutable state can lead to complex bugs and race conditions.
 *
 * Example usage:
 *
 * {@code
 * @Immutable
 * public class Point {
 *     private final int x;
 *     private final int y;
 *
 *     public Point(int x, int y) {
 *         this.x = x;
 *         this.y = y;
 *     }
 *
 *     public int getX() {
 *         return x;
 *     }
 *
 *     public int getY() {
 *         return y;
 *     }
 * }
 * }
 *
 * In this example, the 'Point' class is annotated with '@Immutable', indicating that it is immutable.
 * The class has two final fields 'x' and 'y', and they are assigned values only once during object creation.
 * There are no setter methods, and the fields are accessed only through getter methods, ensuring that the
 * object's state cannot be modified after creation.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Immutable {
}
