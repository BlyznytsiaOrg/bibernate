package io.github.blyznytsiaorg.bibernate.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

class BibernateConfigurationTest {
    public static final String PROPERTIES_FILE_NAME = "test.properties";
    public static final String DB_PASSWORD_PROPERTY_NAME = "db.password";
    public static final String DB_USER_PROPERTY_NAME = "db.user";
    public static final String DB_URL_PROPERTY_NAME = "db.url";
    private static BibernateConfiguration bibernateConfiguration;

    @BeforeAll
    static void beforeAll() {
        bibernateConfiguration = new BibernateConfiguration(PROPERTIES_FILE_NAME);
    }

    @Test
    @DisplayName("should read properties as env variables")
    void shouldReadProperties() {
        // given
        String url = "jdbc:postgresql://localhost:5432/db";
        String user = "user";
        String password = "password";

        // when
        Map<String, String> loadedProperties = bibernateConfiguration.load();
        String loadedPassword = loadedProperties.get(DB_PASSWORD_PROPERTY_NAME);
        String loadedUser = loadedProperties.get(DB_USER_PROPERTY_NAME);
        String loadedUrl = loadedProperties.get(DB_URL_PROPERTY_NAME);

        //then
        assertThat(loadedPassword).isEqualTo(password);
        assertThat(loadedUser).isEqualTo(user);
        assertThat(loadedUrl).isEqualTo(url);
    }
}