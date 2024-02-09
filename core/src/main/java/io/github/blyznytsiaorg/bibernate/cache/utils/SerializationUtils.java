package io.github.blyznytsiaorg.bibernate.cache.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * Utility class for serializing and deserializing objects using Kryo serialization.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class SerializationUtils {

    /**
     * Serializes the provided entity object into a byte array using Kryo serialization.
     *
     * @param entityClass the class of the entity object
     * @param entity      the entity object to serialize
     * @param <T>         the type of the entity object
     * @return the byte array representing the serialized entity
     */
    public static <T> byte[] serialize(Class<T> entityClass, T entity) {
        Kryo kryo = new Kryo();
        kryo.register(entityClass);

        try (Output output = new Output(4096, -1)) {
            kryo.writeObject(output, entity);
            return output.toBytes();
        }
    }

    /**
     * Deserializes the provided byte array into an object of the specified entity class using Kryo deserialization.
     *
     * @param entityClass the class of the entity object
     * @param entity      the byte array representing the serialized entity
     * @param <T>         the type of the entity object
     * @return an Optional containing the deserialized entity object, or an empty Optional if deserialization fails
     */
    public static <T> Optional<T> deserialize(Class<T> entityClass, byte[] entity) {
        Kryo kryo = new Kryo();
        kryo.register(entityClass);

        try (Input input = new Input(entity)) {
            return Optional.ofNullable(kryo.readObject(input, entityClass));
        }
    }
}
