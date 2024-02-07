package io.github.blyznytsiaorg.bibernate.session;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BibernateSessionContextHolder {
    private static final ThreadLocal<BibernateSession> sessionContextHolder = new ThreadLocal<>();

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
