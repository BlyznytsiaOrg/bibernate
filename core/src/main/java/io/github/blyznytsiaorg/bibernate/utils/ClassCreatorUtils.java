package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.exception.ClassLimitationCreationException;
import lombok.experimental.UtilityClass;


/**
 * Utility class for creating new instances of classes using reflection.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class ClassCreatorUtils {

    /**
     * Creates a new instance of the specified class using reflection.
     *
     * @param <T>          The type of the class to be instantiated.
     * @param aClass       The class to instantiate.
     * @param errorMessage The error message to be used if the instantiation fails.
     * @return A new instance of the specified class.
     * @throws ClassLimitationCreationException If there is an issue creating the class instance.
     */
    public static <T>  T createNewInstance(Class<?> aClass, String errorMessage) {
        try {
            return (T) aClass.getDeclaredConstructor().newInstance();
        } catch (Exception exe) {
            throw new ClassLimitationCreationException(errorMessage.formatted(aClass.getSimpleName(), exe.getMessage(), exe));
        }
    }
}
