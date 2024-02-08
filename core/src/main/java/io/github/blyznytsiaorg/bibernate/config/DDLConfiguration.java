package io.github.blyznytsiaorg.bibernate.config;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.ddl.DDLProcessor;
import io.github.blyznytsiaorg.bibernate.ddl.DDLQueryCreator;
import io.github.blyznytsiaorg.bibernate.ddl.EntityMetadataCollector;

public class DDLConfiguration {

    public DDLConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings,
                            String packageName) {
        processBb2DdlProperty(bibernateDatabaseSettings, packageName);
    }

    private void processBb2DdlProperty(BibernateDatabaseSettings bibernateDatabaseSettings, String packageName) {
        if (bibernateDatabaseSettings.isDDLCreate()) {
            HikariDataSource dataSource = bibernateDatabaseSettings.getDataSource();
            EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(packageName);
            DDLProcessor ddlProcessor = new DDLProcessor(new DDLQueryCreator(entityMetadataCollector), dataSource);
            ddlProcessor.processCreateProperty();
        }
    }
}
