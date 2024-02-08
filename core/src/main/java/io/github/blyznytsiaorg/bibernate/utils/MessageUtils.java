package io.github.blyznytsiaorg.bibernate.utils;

import lombok.experimental.UtilityClass;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class MessageUtils {

    @UtilityClass
    public static class ExceptionMessage {
        public static final String CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS =
                "Cannot execute findById entityClass [%s]. Message: %s";
        public static final String CANNOT_EXECUTE_UPDATE_ENTITY_CLASS =
                "Cannot execute update entityClass [%s] for primaryKey %s. Message: %s";
        public static final String CANNOT_EXECUTE_SAVE_ENTITY_CLASS =
                "Cannot execute save entityClass [%s]. Message: %s";
        public static final String CANNOT_EXECUTE_DELETE_ENTITY_CLASS =
                "Cannot execute delete entityClass [%s] with primaryKey %s. Message: %s";
        public static final String CANNOT_EXECUTE_DELETE_ENTITY_CLASS_ALL_BY_ID =
                "Cannot execute delete entityClass [%s] with primaryKeys [%s]. Message: %s";
        public static final String CANNOT_EXECUTE_QUERY = "Cannot execute query %s. Message: %s";
        public static final String ENTITY_CLASS_MUST_BE_NOT_NULL = "EntityClass must be not null";
        public static final String FIELD_MUST_BE_NOT_NULL = "Field must be not null";
        public static final String ENTITY_MUST_BE_NOT_NULL = "Entity must be not null";
        public static final String PRIMARY_KEY_MUST_BE_NOT_NULL = "PrimaryKey must be not null";
        public static final String COLLECTION_MUST_BE_NOT_EMPTY = "Collection must be not empty";
        public static final String NON_UNIQUE_RESULT_FOR_FIND_BY_ID = "Non-unique result for findById on [%s]";
        public static final String ENTITY_WAS_CHANGE_NEED_TO_GET_NEW_DATA = "Entity %s was change need to get new data findBy%s[%s]";
        public static final String CANNOT_FIND_SEQUENCE_STRATEGY =
            "Cannot find SEQUENCE strategy for entity entityClass [%s]";
        public static final String CANNOT_GET_ID_FROM_SEQUENCE =
            "Cannot get Id for sequence: [%s]";
    }

    @UtilityClass
    public static class LogMessage {
        public static final String QUERY = "Query {}";
        public static final String QUERY_BIND_VALUE = QUERY + " bindValue {}={}";
        public static final String QUERY_BIND_TWO_VALUES = QUERY + " bindValue {}={}, {}={}";
        public static final String QUERY_BIND_VALUES = QUERY + " bindValues {}";
        public static final String UPDATE = "Update effected row {} for entity clazz {} with id {}";
        public static final String SAVE = "Save entity clazz {}";
        public static final String DELETE = "Delete entity {} with {}={}";
        public static final String DELETE_ALL = "Delete entity {} with primaryKeys {}, batch size {}.";
        public static final String ENTITY_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID =
                "Entity {} found in firstLevel cache by id {}";
        public static final String SESSION_IS_CLOSING_PERFORMING_DIRTY_CHECKING =
                "Session is closing. Performing dirty checking...";
        public static final String FIRST_LEVEL_CACHE_IS_CLEARING = "FirstLevelCache is clearing...";
        public static final String SNAPSHOTS_ARE_CLEARING = "Snapshots are clearing...";
        public static final String ENTITY_NOT_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID =
                "Entity {} not found in firstLevel cache by id {}";
        public static final String DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_FIRST_LEVEL_CACHE =
                "Deleted entityClass [{}] with primaryKey {} from firstLevelCache";
        public static final String DELETED_ENTITY_CLASS_WITH_PRIMARY_KEYS_FROM_FIRST_LEVEL_CACHE =
                "Deleted entityClass [{}] with primaryKeys {} from firstLevelCache";
        public static final String DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_SNAPSHOT =
                "Deleted entityClass [{}] with primaryKey {} from snapshot";
        public static final String DELETED_ENTITY_CLASS_WITH_PRIMARY_KEYS_FROM_SNAPSHOT =
                "Deleted entityClass [{}] with primaryKeys {} from snapshot";
        public static final String UPDATE_ENTITY_IN_FIRST_LEVEL_CACHE_BY_ID =
                "Update Entity {} in firstLevel cache by id {}";
        public static final String UPDATE_SNAPSHOT_FOR_ENTITY_ID = "Update snapshot for entity {} id {}";
        public static final String DIRTY_ENTITY_FOUND_NEED_TO_GENERATE_UPDATE_FOR_ENTITY_KEY_AND_ENTITY =
                "Dirty entity found need to generate update for entityKey {} and entity {}";
        public static final String DIRTY_ENTITY_NOT_FOUND_FOR_ENTITY_KEY_NO_CHANGES =
                "Dirty entity not found for entityKey {} no changes";
        public static final String CREATED_SNAPSHOT_FOR_ENTITY_ID = "Created snapshot for entity {} id {}";
        public static final String CLOSE_SESSION = "Close session...";
    }

}
