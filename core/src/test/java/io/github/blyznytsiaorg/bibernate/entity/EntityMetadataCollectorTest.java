package io.github.blyznytsiaorg.bibernate.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.findbyid.Address;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMetadataCollectorTest {

    @DisplayName("Should collect metadata for all entities")
    @Test
    void shouldCollectMetadataForEntities() {
        //given
        var entityMetadataCollector = new EntityMetadataCollector("testdata.findbyid");

        //when
        entityMetadataCollector.startCollectMetadata();

        //then
        var inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();

        assertThat(inMemoryEntityMetadata).isNotNull();
        assertThat(inMemoryEntityMetadata).hasSize(4);

        EntityMetadata entityMetadata = inMemoryEntityMetadata.get(Address.class);

        assertThat(entityMetadata).isNotNull();
        assertThat(entityMetadata.getTableName()).isEqualTo("addresses");
        assertThat(entityMetadata.isDynamicUpdate()).isFalse();
        assertThat(entityMetadata.isImmutable()).isFalse();

        assertThat(entityMetadata.getEntityColumns()).hasSize(3);

        boolean isIdFound = false;

        for (EntityColumnDetails entityColumnDetails : entityMetadata.getEntityColumns()) {
            if (entityColumnDetails.isColumnId())  {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("id");
                assertThat(entityColumnDetails.getFieldColumnName()).isEqualTo("id");
                isIdFound = true;
            } else if (entityColumnDetails.getFieldName().equals("house")){
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("house");
                assertThat(entityColumnDetails.getFieldColumnName()).isEqualTo("house");
                assertThat(entityColumnDetails.isJoinColumn()).isTrue();
                assertThat(entityColumnDetails.isOneToOne()).isTrue();
            } else if (entityColumnDetails.getFieldName().equals("name")) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("name");
                assertThat(entityColumnDetails.getFieldColumnName()).isEqualTo("name");
                assertThat(entityColumnDetails.isJoinColumn()).isFalse();
                assertThat(entityColumnDetails.isOneToOne()).isFalse();
            }
        }

        assertThat(isIdFound).isTrue();
    }
}