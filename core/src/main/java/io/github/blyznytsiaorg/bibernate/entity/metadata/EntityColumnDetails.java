package io.github.blyznytsiaorg.bibernate.entity.metadata;

import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.GeneratedValueMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IdMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinTableMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.SequenceGeneratorMetadata;
import lombok.Builder;
import lombok.Getter;
import java.lang.reflect.Field;

@Getter
@Builder
public class EntityColumnDetails {
    private Field field;
    private String fieldName;
    private Class<?> fieldType;
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
}
