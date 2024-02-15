package io.github.blyznytsiaorg.bibernate.connectionpool;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * BibernateDatasSourceConfig represents the configuration for BibernateDataSource.
 * It includes properties such as JDBC URL, username, password and maximum pool size.
 *
 * @see BibernateDataSource
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Setter
@Getter
@Builder
public class BibernateDatasSourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int maximumPoolSize;
}
