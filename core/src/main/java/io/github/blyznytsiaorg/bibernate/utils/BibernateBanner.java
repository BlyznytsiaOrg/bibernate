package io.github.blyznytsiaorg.bibernate.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * The {@code Banner} class provides functionality for printing a banner to the console,
 * either from a predefined string or custom banner by reading from a file.
 * <p>
 * Configuration options:
 * <ul>
 *     <li>{@link #BIBERNATE_BANNER_KEY}: Key for enabling/disabling the main banner. Default is "ON".</li>
 * </ul>
 *
 * @see BibernateBanner.Mode
 */

@Slf4j
@UtilityClass
public class BibernateBanner {

    /**
     * Key for enabling/disabling the main banner. Default is "ON".
     */
    public static final String BIBERNATE_BANNER_KEY = "bibernate.main.banner";

    /**
     * Default value for enabling/disabling the main banner.
     */
    public static final String BIBERNATE_BANNER_VALUE = "OFF";

    /**
     * The bibernate banner content.
     */

    public static final String BIBERNATE_BANNER = """
              \033[1;94m
              ____  _ _                           _        ______ \s
             | __ )(_) |__   ___ _ __ _ __   __ _| |_ ___  \\ \\ \\ \\\s
             |  _ \\| | '_ \\ / _ \\ '__| '_ \\ / _` | __/ _ \\  | | | |\033[1;93m
             | |_) | | |_) |  __/ |  | | | | (_| | ||  __/  | | | |
             |____/|_|_.__/ \\___|_|  |_| |_|\\__,_|\\__\\___|  | | | |
             \033[1;97m            :: By Blyznytsia ::\033[1;93m               /_/_/_/\s
             \033[0m
            """;

    /**
     * Prints the banner to the console based on the configured mode and file settings.
     */
    public static void printBanner() {
        Mode bannerMode = getBannerMode();
        if (bannerMode == Mode.ON) {
                System.out.println(BIBERNATE_BANNER);
            }
        }

    /**
     * Gets the configured banner mode (ON or OFF).
     *
     * @return The configured banner mode.
     */
    private static Mode getBannerMode() {
        String propertyBannerMode = System.getProperty(BIBERNATE_BANNER_KEY);
        return BIBERNATE_BANNER_VALUE.equalsIgnoreCase(propertyBannerMode) ? Mode.OFF : Mode.ON;
    }

    /**
     * An enumeration of possible values for configuring the Banner.
     */
    public enum Mode {
        /**
         * Disable printing of the banner.
         */
        OFF,

        /**
         * Print the banner to System.out.
         */
        ON
    }
}
