package io.github.blyznytsiaorg.bibernate.session;

import org.reflections.Reflections;


/**
 * Utility class for managing thread-local instances of the Reflections library,
 * which is used for classpath scanning and metadata retrieval.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateReflectionHolder {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private BibernateReflectionHolder() {
    }

    /**
     * Thread-local instance of Reflections for classpath scanning and metadata retrieval.
     */
    private static final ThreadLocal<Reflections> reflectionsThreadLocal = new ThreadLocal<>();

    /**
     * Gets the thread-local instance of Reflections.
     *
     * @return The Reflections instance for the current thread.
     */
    public static Reflections getReflections() {
        return reflectionsThreadLocal.get();
    }

    /**
     * Sets the thread-local instance of Reflections based on the provided internal package for classpath scanning.
     *
     * @param internalPackage The internal package to use for classpath scanning.
     */
    public static void setReflection(String internalPackage) {
        reflectionsThreadLocal.set(new Reflections(internalPackage));
    }
}
