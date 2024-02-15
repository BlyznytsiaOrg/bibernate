package io.github.blyznytsiaorg.bibernate.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Utility class for generating foreign key constraint names and index names.
 * Provides methods to generate unique names for constraints and indexes.
 *
 * @see io.github.blyznytsiaorg.bibernate.ddl.DDLQueryCreator
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class DDLUtils {
    private static final char[] setOfChars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
           'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static final String FK_PATTERN = "FK_%s";
    public static final String INDEX_PATTERN = "IDX_%s";

    /**
     * Generates a unique foreign key constraint name.
     *
     * @return a unique foreign key constraint name
     */
    public static String getForeignKeyConstraintName() {
        String randomNumber = RandomStringUtils.random(12, 0, 35, true, true, setOfChars);
        return FK_PATTERN.formatted(randomNumber);
    }

    /**
     * Generates a unique index name.
     *
     * @return a unique index name
     */
    public static String getIndexName() {
        String randomNumber = RandomStringUtils.random(12, 0, 35, true, true, setOfChars);
        return INDEX_PATTERN.formatted(randomNumber);
    }
}
