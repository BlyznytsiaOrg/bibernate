package io.github.blyznytsiaorg.bibernate.config;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.ddl.DDLProcessor;
import io.github.blyznytsiaorg.bibernate.ddl.DDLQueryCreator;

/**
 * Configuration class responsible for processing DDL (Data Definition Language) properties
 * based on the provided BibernateDatabaseSettings. It supports creating DDL statements
 * when the corresponding property is set to true in the settings.
 * The class uses a DDLProcessor to handle the creation of DDL queries.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class DDLConfiguration {

    /**
     * Constructs a new DDLConfiguration instance with the given BibernateDatabaseSettings.
     * Processes DDL properties and creates DDL statements if the corresponding property is set to true.
     *
     * @param bibernateDatabaseSettings the settings containing DDL-related properties
     */
    public DDLConfiguration(BibernateDatabaseSettings bibernateDatabaseSettings) {
        processBb2DdlProperty(bibernateDatabaseSettings);
    }

    /**
     * Processes the DDL property from the provided BibernateDatabaseSettings.
     * If the property is set to true, creates DDL statements using a DDLProcessor.
     *
     * @param bibernateDatabaseSettings the settings containing DDL-related properties
     */
    private void processBb2DdlProperty(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isDDLCreate()) {
            var dataSource = bibernateDatabaseSettings.getDataSource();
            var ddlProcessor = new DDLProcessor(new DDLQueryCreator(), dataSource);
            ddlProcessor.processCreateProperty();
        }
    }
}
