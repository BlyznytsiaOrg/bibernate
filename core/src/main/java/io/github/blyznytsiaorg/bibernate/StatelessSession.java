package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;

public class StatelessSession {
    private final BibernateDatabaseSettings bibernateDatabaseSettings;

    public StatelessSession(BibernateDatabaseSettings bibernateDatabaseSettings) {
        this.bibernateDatabaseSettings = bibernateDatabaseSettings;
    }
}
