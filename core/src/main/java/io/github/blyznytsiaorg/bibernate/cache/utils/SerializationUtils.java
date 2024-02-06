package io.github.blyznytsiaorg.bibernate.cache.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class SerializationUtils {

    public static <T> byte[] serialize(Class<T> entityClass, T entity) {
        Kryo kryo = new Kryo();
        kryo.register(entityClass);

        try (Output output = new Output(4096, -1)) {
            kryo.writeObject(output, entity);
            return output.toBytes();
        }
    }

    public static <T> Optional<T> deserialize(Class<T> entityClass, byte[] entity) {
        Kryo kryo = new Kryo();
        kryo.register(entityClass);

        try (Input input = new Input(entity)) {
            return Optional.ofNullable(kryo.readObject(input, entityClass));
        }
    }
}
