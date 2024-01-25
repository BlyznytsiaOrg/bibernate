package io.github.blyznytsiaorg.bibernate.config;

import io.github.blyznytsiaorg.bibernate.utils.PropertyParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@Slf4j
public class BibernateConfiguration {
    
    private static final String DEFAULT_CONFIGURATION = "bibernate.properties";
    private final String configFileName;
    
    public BibernateConfiguration(String configFileName) {
        this.configFileName = configFileName;
    }

    public BibernateConfiguration() {
        this.configFileName = DEFAULT_CONFIGURATION;
    }

    /**
     * Loads properties from a specified configFileName.
     *
     * @return A map containing the properties loaded from the specified file.
     */
    public Map<String, String> load() {
        log.debug("Load Property file {}", configFileName);
        Map<String, String> propertiesMap = new HashMap<>();

        try (var source = BibernateConfiguration.class.getClassLoader().getResourceAsStream(configFileName)) {
            if (Objects.isNull(source)) {
                log.warn("Cannot find file {}", configFileName);
                return propertiesMap;
            }

            Properties properties = new Properties();
            properties.load(source);

            propertiesMap = properties.stringPropertyNames()
                    .stream()
                    .collect(toMap(key -> key, getValue(properties), (a, b) -> b));

        } catch (IOException exe) {
            log.error("Error loading properties from file {}: message {}", configFileName,
                    exe.getMessage(), exe);
        }

        return propertiesMap;
    }

    private Function<String, String> getValue(Properties properties) {
        return key -> PropertyParser.processProperty(properties.getProperty(key));
    }
}
