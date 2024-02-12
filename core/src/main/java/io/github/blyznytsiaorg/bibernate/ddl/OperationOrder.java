package io.github.blyznytsiaorg.bibernate.ddl;

import lombok.experimental.UtilityClass;

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
