package io.github.blyznytsiaorg.bibernate.config;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.ddl.DDLProcessor;
import io.github.blyznytsiaorg.bibernate.ddl.DDLQueryCreator;


public class DDLConfiguration {

    public DDLConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings) {
        processBb2DdlProperty(bibernateDatabaseSettings);
    }

    private void processBb2DdlProperty(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isDDLCreate()) {
            BibernateDataSource dataSource = bibernateDatabaseSettings.getDataSource();
            DDLProcessor ddlProcessor = new DDLProcessor(new DDLQueryCreator(), dataSource);
            ddlProcessor.processCreateProperty();
        }
    }
}
