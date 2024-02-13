package io.github.blyznytsiaorg.bibernate.connectionpool;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class BibernateDatasSourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int maximumPoolSize;
}
