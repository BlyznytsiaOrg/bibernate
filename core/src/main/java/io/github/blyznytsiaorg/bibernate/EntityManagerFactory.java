package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;

import java.io.Closeable;

/**
 * Interface representing a factory for creating EntityManager instances in the Bibernate framework.
 * An EntityManagerFactory is used to obtain EntityManager instances, which provide access to the underlying persistence context.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface EntityManagerFactory extends Closeable {

    /**
     * Retrieves the BibernateSessionFactory associated with this EntityManagerFactory.
     *
     * @return the BibernateSessionFactory instance
     */
    BibernateSessionFactory getBibernateSessionFactory();

    /**
     * Retrieves the SimpleRepositoryInvocationHandler associated with this EntityManagerFactory.
     * The SimpleRepositoryInvocationHandler is responsible for handling method invocations on simple repository interfaces.
     *
     * @return the SimpleRepositoryInvocationHandler instance
     */
    SimpleRepositoryInvocationHandler getSimpleRepositoryInvocationHandler();
}
