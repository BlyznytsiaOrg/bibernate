package io.github.blyznytsiaorg.bibernate.entity.type;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TypeResolverFactory {
    private final List<TypeFieldResolver> typeFieldResolvers = new ArrayList<>();

    public TypeResolverFactory() {
        this.typeFieldResolvers.add(new EntityFieldFieldResolver());
        this.typeFieldResolvers.add(new RegularFieldFieldResolver());
    }
}
