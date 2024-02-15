package io.github.blyznytsiaorg.bibernate.ddl;

import lombok.experimental.UtilityClass;

/**
 * The OperationOrder utility class defines constants representing the order of database operations for
 * Data Definition Language (DDL) queries.
 * <p>
 * By following the defined order, dependencies between DDL operations are managed to ensure
 * proper execution of database schema creation and migrations.
 *
 * @see DDLQueryCreator
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class OperationOrder {
    public static final int DROP_CONSTRAINT = 1;
    public static final int DROP_TABLE = 2;
    public static final int DROP_SEQUENCE = 3;
    public static final int CREATE_SEQUENCE = 4;
    public static final int CREATE_TABLE = 5;
    public static final int CREATE_INDEX = 6;
    public static final int CREATE_CONSTRAINT = 7;
}
