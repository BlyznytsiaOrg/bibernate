package io.github.blyznytsiaorg.bibernate.ddl.model;

public record ColumnData(String name, String type, String size, String definition,
                  boolean autoIncrement, boolean nullable) {
}
