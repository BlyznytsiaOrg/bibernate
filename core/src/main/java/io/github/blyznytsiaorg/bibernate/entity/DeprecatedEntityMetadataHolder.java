package io.github.blyznytsiaorg.bibernate.entity;

import java.util.Optional;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.experimental.UtilityClass;

@Deprecated(forRemoval = true)
@UtilityClass
public class DeprecatedEntityMetadataHolder {

  private static final String UNABLE_TO_FIND_METADATA_FOR_ENTITY = "Unable to find metadata for Entity [%s]";
  
  public EntityMetadata getEntityMetadata(Class<?> clazz) {
    EntityMetadataCollector collector = new EntityMetadataCollector(clazz);
    collector.collectMetadata();
    
    return Optional.ofNullable(collector.getInMemoryEntityMetadata().get(clazz))
      .orElseThrow(() -> new BibernateGeneralException(UNABLE_TO_FIND_METADATA_FOR_ENTITY.formatted(clazz.getName())));
  }
}
