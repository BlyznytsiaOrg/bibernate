package io.github.blyznytsiaorg.bibernate.session;

public class BibernateSessionFactoryContextHolder {

    private BibernateSessionFactoryContextHolder(){}
    private static final ThreadLocal<BibernateSessionFactory> sessionContextHolder = new ThreadLocal<>();

    public static BibernateSessionFactory getBibernateSessionFactory() {
        return sessionContextHolder.get();
    }

    public static void setBibernateSessionFactory(BibernateSessionFactory bibernateSessionFactory) {
        sessionContextHolder.set(bibernateSessionFactory);
    }

    public static void resetBibernateSessionFactory() {
        sessionContextHolder.remove();
    }
}
