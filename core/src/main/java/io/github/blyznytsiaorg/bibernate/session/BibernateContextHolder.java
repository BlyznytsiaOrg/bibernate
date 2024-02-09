package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.util.Map;


/**
 * Internal usage for repositories etc.
 * Utility class for managing thread-local instances of the Reflections library,
 * which is used for classpath scanning and metadata retrieval.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class BibernateContextHolder {

    /**
     * Thread-local instance of Reflections for classpath scanning and metadata retrieval.
     */
    private static final ThreadLocal<Reflections> reflectionsThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Map<Class<?>, EntityMetadata>> entityMetadataContextHolder = new ThreadLocal<>();

    private static final ThreadLocal<BibernateSession> sessionContextHolder = new ThreadLocal<>();

    private static final ThreadLocal<BibernateSessionFactory> sessionFactoryContextHolder = new ThreadLocal<>();

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

    /**
     * Retrieves the entity metadata stored in the current thread's context.
     *
     * @return the map containing entity metadata, where the keys are entity classes and the values are corresponding metadata
     */
    public static Map<Class<?>, EntityMetadata> getBibernateEntityMetadata() {
        return entityMetadataContextHolder.get();
    }

    /**
     * Sets the entity metadata in the current thread's context.
     *
     * @param entityMetadata the map containing entity metadata, where the keys are entity classes and the values are corresponding metadata
     */
    public static void setBibernateEntityMetadata(Map<Class<?>, EntityMetadata> entityMetadata) {
        entityMetadataContextHolder.set(entityMetadata);
    }

    public static BibernateSession getBibernateSession() {
        return sessionContextHolder.get();
    }

    public static void setBibernateSession(BibernateSession bibernateSession) {
        sessionContextHolder.set(bibernateSession);
    }

    public static void resetBibernateSession() {
        sessionContextHolder.remove();
    }

    public static BibernateSessionFactory getBibernateSessionFactory() {
        return sessionFactoryContextHolder.get();
    }

    public static void setBibernateSessionFactory(BibernateSessionFactory bibernateSessionFactory) {
        sessionFactoryContextHolder.set(bibernateSessionFactory);
    }

    public static void resetBibernateSessionFactory() {
        sessionFactoryContextHolder.remove();
    }

}
