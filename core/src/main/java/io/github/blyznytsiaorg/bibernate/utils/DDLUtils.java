package io.github.blyznytsiaorg.bibernate.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

@UtilityClass
public class DDLUtils {
   private static final char[] setOfChars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
           'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static String getForeignKeyConstraintName() {
        String randomNumber = RandomStringUtils.random(12, 0, 35, true, true, setOfChars);
        return "FK_%s".formatted(randomNumber);
    }

    public static String getIndexName() {
        String randomNumber = RandomStringUtils.random(12, 0, 35, true, true, setOfChars);
        return "IDX_%s".formatted(randomNumber);
    }
}
