package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.exception.FailedToMatchPropertyException;
import lombok.experimental.UtilityClass;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing properties, including resolving environment variables.
 * This class provides methods to process property values, especially those containing environment variable references.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class PropertyParser {
    private static final String PATTERN = "\\$\\{(.*?)}";
    private static final String PREFIX = "$";
    private static final Pattern COMPILE_PARAM = Pattern.compile(PATTERN);
    public static final String FORMATTED_MESSAGE = "Can't match a pattern to read a property value '%s'"
            + "for extracting env variable";

    /**
     * Processes the provided property value, resolving environment variables if needed.
     *
     * @param propertyValue the property value to process
     * @return the processed property value
     */
    public static String processProperty(String propertyValue) {
        if (propertyValue.startsWith(PREFIX)) {
            return getEnvValue(propertyValue);
        }
        return propertyValue;
    }

    /**
     * Retrieves the value of the environment variable specified in the property value.
     *
     * @param propertyValue the property value containing the environment variable reference
     * @return the value of the resolved environment variable
     * @throws FailedToMatchPropertyException if the property value does not match the expected pattern
     */
    private static String getEnvValue(String propertyValue) {

        Matcher matcher = COMPILE_PARAM.matcher(propertyValue);

        if (matcher.find()) {
            String key = matcher.group(1);
            return System.getenv(key);
        }

        throw new FailedToMatchPropertyException(
                FORMATTED_MESSAGE.formatted(propertyValue));
    }
}
