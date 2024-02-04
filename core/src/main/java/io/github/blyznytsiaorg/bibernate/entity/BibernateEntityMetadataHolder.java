package io.github.blyznytsiaorg.bibernate.entity;

import java.util.Map;

public class BibernateEntityMetadataHolder {
    private BibernateEntityMetadataHolder() {
    }

    private static final ThreadLocal<Map<Class<?>, EntityMetadata>> entityMetadataContextHolder = new ThreadLocal<>();

    public static Map<Class<?>, EntityMetadata> getBibernateEntityMetadata() {
        return entityMetadataContextHolder.get();
    }

    public static void setBibernateEntityMetadata(Map<Class<?>, EntityMetadata> entityMetadata) {
        entityMetadataContextHolder.set(entityMetadata);
    }

}
