package io.github.blyznytsiaorg.bibernate.entity.metadata;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter
@Setter
@Builder
public class EntityColumnDetails {
    private Field field;
    private String fieldName;
    private String table;
    private Class<?> fieldType;
    private boolean isCollection;
    private ColumnMetadata column;
    private IdMetadata id;
    private GeneratedValueMetadata generatedValue;
    private SequenceGeneratorMetadata sequenceGenerator;
    private OneToOneMetadata oneToOne;
    private OneToManyMetadata oneToMany;
    private ManyToOneMetadata manyToOne;
    private ManyToManyMetadata manyToMany;
    private JoinColumnMetadata joinColumn;
    private JoinTableMetadata joinTable;
    private CreationTimestampMetadata creationTimestampMetadata;
    private UpdateTimestampMetadata updateTimestampMetadata;

    public List<CascadeType> getCascadeTypes() {
        List<CascadeType> cascadeTypes = new ArrayList<>();
        addCascadeTypes(cascadeTypes, OneToOneMetadata::getCascadeTypes, oneToOne);
        addCascadeTypes(cascadeTypes, OneToManyMetadata::getCascadeTypes, oneToMany);
        addCascadeTypes(cascadeTypes, ManyToOneMetadata::getCascadeTypes, manyToOne);
        addCascadeTypes(cascadeTypes, ManyToManyMetadata::getCascadeTypes, manyToMany);
        return cascadeTypes;
    }

    private <T> void addCascadeTypes(List<CascadeType> types, Function<T, List<CascadeType>> function, T obj) {
        Optional.ofNullable(obj).ifPresent(o -> types.addAll(function.apply(obj)));
    }
}
