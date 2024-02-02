package io.github.blyznytsiaorg.bibernate.session;

public class BibernateSessionContextHolder {
    private static final ThreadLocal<BibernateSession> sessionContextHolder = new ThreadLocal<>();

    private BibernateSessionContextHolder() {
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
}
