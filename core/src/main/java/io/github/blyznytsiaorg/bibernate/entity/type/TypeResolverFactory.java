package io.github.blyznytsiaorg.bibernate.entity.type;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating type field resolvers based on the type of field encountered during custom injection.
 * <p>
 * This factory initializes a list of {@link TypeFieldResolver} implementations to handle different types of fields in an entity class.
 */
@Getter
public class TypeResolverFactory {
    private final List<TypeFieldResolver> typeFieldResolvers = new ArrayList<>();

    /**
     * Constructs a new {@code TypeResolverFactory} and initializes the list of type field resolvers.
     * The default resolvers added to the factory are:
     *
     *     <li>{@link OneToOneEagerFieldResolver}</li>
     *     <li>{@link LazyEntityFieldResolver}</li>
     *     <li>{@link EntityFieldResolver}</li>
     *     <li>{@link RegularFieldFieldResolver}</li>
     *     <li>{@link CollectionFieldResolver}</li>
     */
    public TypeResolverFactory() {
        this.typeFieldResolvers.add(new OneToOneEagerFieldResolver());
        this.typeFieldResolvers.add(new LazyEntityFieldResolver());
        this.typeFieldResolvers.add(new EntityFieldResolver());
        this.typeFieldResolvers.add(new RegularFieldFieldResolver());
        this.typeFieldResolvers.add(new CollectionFieldResolver());
    }
}
