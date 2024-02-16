package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A configuration class for managing and providing sequences of IDs in a Bibernate application.
 * This class is designed to handle the generation and allocation of unique IDs using a queue-based approach.
 * The sequence configuration includes parameters such as name, initial value, and allocation size.
 * The default initial value and allocation size are set to 1, and the default sequence template is used for generating sequence names.
 * <p>
 * The class provides methods to retrieve the next ID from the sequence and set the next portion of IDs based on the current ID.
 * The queue-based approach allows efficient and controlled allocation of IDs for entities in the application.
 * <p>
 * Note: The sequence name is typically constructed using the default sequence template,
 * where the placeholders are replaced with the table name and column name.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
public class SequenceConf {

    /**
     * The queue to store and manage generated IDs.
     */
    private Queue<Long> ids = new LinkedList<>();

    /**
     * The default initial value for the sequence.
     */
    private static final int DEFAULT_INITIAL_VALUE = 1;

    /**
     * The default allocation size for the sequence.
     */
    private static final int DEFAULT_ALLOCATION_SIZE = 1;

    /**
     * The default sequence template for generating sequence names.
     * It includes placeholders for table name and column name.
     */
    public static final String DEFAULT_SEQ_TEMPLATE = "%s_%s_seq"; //tableName_columnName_seq

    /**
     * The name of the sequence.
     */
    @Getter
    private final String name;

    /**
     * The initial value for the sequence.
     */
    private final int initialValue;

    /**
     * The allocation size for the sequence.
     */
    private final int allocationSize;

    /**
     * Constructs a new SequenceConf with the given name, using default initial value and allocation size.
     *
     * @param name the name of the sequence
     */
    public SequenceConf(String name) {
        this.name = name;
        this.initialValue = DEFAULT_INITIAL_VALUE;
        this.allocationSize = DEFAULT_ALLOCATION_SIZE;
    }

    /**
     * Retrieves the next ID from the sequence.
     *
     * @return the next ID
     */
    public Long getNextId() {
        return ids.poll();
    }

    /**
     * Sets the next portion of IDs in the sequence based on the current ID.
     *
     * @param currentId the current ID
     */
    public void setNextPortionOfIds(Long currentId) {
        for (long i = currentId - allocationSize + 1; i <= currentId; i++) {
            ids.add(i);
        }
    }
}
