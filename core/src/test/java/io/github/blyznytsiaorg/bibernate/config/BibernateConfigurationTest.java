package io.github.blyznytsiaorg.bibernate.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import java.util.Map;

@ExtendWith(SystemStubsExtension.class)
class BibernateConfigurationTest {
    private static final String PROPERTIES_FILE_NAME = "test.properties";
    private static final String DB_PASSWORD_PROPERTY_NAME = "db.password";
    private static final String DB_USER_PROPERTY_NAME = "db.user";
    private static final String DB_URL_PROPERTY_NAME = "db.url";
    private static final String url = "jdbc:postgresql://localhost:5432/db";
    private static final String user = "user";
    private static final String password = "password";
    private static BibernateConfiguration bibernateConfiguration;
    @SystemStub
    private static EnvironmentVariables environmentVariables;

    @BeforeAll
    static void beforeAll() {
        environmentVariables.set("DB.PASSWORD", "password");
        bibernateConfiguration = new BibernateConfiguration(PROPERTIES_FILE_NAME);

    }

    @Test
    @Disabled("Need to find way to read env variables during maven build")
    @DisplayName("should read properties as env variables")
    void shouldReadProperties() {
        // given

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