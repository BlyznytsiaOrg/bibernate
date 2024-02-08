package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.ddl.EntityMetadataCollector;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.entity.Author;
import testdata.entity.Book;
import testdata.entity.Phone;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

class EntityMetadataCollectorTest {
    private static Map<Class<?>, EntityMetadata> inMemoryEntityMetadata;

    @BeforeAll
    static void beforeAll() {
        var entityMetadataCollector = new EntityMetadataCollector("testdata.entity");
        entityMetadataCollector.collectMetadata();
        inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();
    }

    @DisplayName("Should collect metadata for all entities")
    @Test
    void shouldCollectMetadataForEntities() {
        assertThat(inMemoryEntityMetadata).isNotNull()
                .hasSize(4);
    }

    @DisplayName("Should collect metadata for Phone entity")
    @Test
    void shouldCollectMetadataForPhoneEntity() {

        EntityMetadata entityMetadata = inMemoryEntityMetadata.get(Phone.class);

        assertThat(entityMetadata).isNotNull();
        assertThat(entityMetadata.getTableName()).isEqualTo("phone");
        assertThat(entityMetadata.isDynamicUpdate()).isFalse();
        assertThat(entityMetadata.isImmutable()).isFalse();

        assertThat(entityMetadata.getEntityColumns()).hasSize(4);

        boolean isIdFound = false;

        for (EntityColumnDetails entityColumnDetails : entityMetadata.getEntityColumns()) {
            if (entityColumnDetails.getId() != null) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("id");
                assertThat(entityColumnDetails.getColumn().getName()).isEqualTo("id");
                isIdFound = true;
            } else if (entityColumnDetails.getFieldName().equals("home_number")) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("home_number");
                assertThat(entityColumnDetails.getColumn().getName()).isEqualTo("home_number");
            } else if (entityColumnDetails.getFieldName().equals("author")) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("author");
                assertThat(entityColumnDetails.getJoinColumn()).isNotNull();
                assertThat(entityColumnDetails.getJoinColumn().getName()).isEqualTo("author_id");
                assertThat(entityColumnDetails.getManyToOne()).isNotNull();
            }
        }

        assertThat(isIdFound).isTrue();
    }

    @DisplayName("Should collect metadata for Author entity")
    @Test
    void shouldCollectMetadataForAuthorEntity() {

        EntityMetadata entityMetadata = inMemoryEntityMetadata.get(Author.class);

        assertThat(entityMetadata).isNotNull();
        assertThat(entityMetadata.getTableName()).isEqualTo("authors");

        assertThat(entityMetadata.getEntityColumns()).hasSize(4);

        for (EntityColumnDetails entityColumnDetails : entityMetadata.getEntityColumns()) {
             if (entityColumnDetails.getFieldName().equals("books")) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("books");
                assertThat(entityColumnDetails.getManyToMany()).isNotNull();
                assertThat(entityColumnDetails.getManyToMany().getMappedBy()).isEqualTo("authors");
            } else if (entityColumnDetails.getFieldName().equals("phones")) {
                 assertThat(entityColumnDetails.getFieldName()).isEqualTo("phones");
                 assertThat(entityColumnDetails.getColumn().getName()).isEqualTo("phones");
                 assertThat(entityColumnDetails.getOneToMany()).isNotNull();
             }
        }
    }

    @DisplayName("Should collect metadata for Book entity")
    @Test
    void shouldCollectMetadataForBookEntity() {

        EntityMetadata entityMetadata = inMemoryEntityMetadata.get(Book.class);

        assertThat(entityMetadata).isNotNull();
        assertThat(entityMetadata.getTableName()).isEqualTo("book");

        assertThat(entityMetadata.getEntityColumns()).hasSize(2);

        for (EntityColumnDetails entityColumnDetails : entityMetadata.getEntityColumns()) {
            if (entityColumnDetails.getFieldName().equals("authors")) {
                assertThat(entityColumnDetails.getFieldName()).isEqualTo("authors");
                assertThat(entityColumnDetails.getManyToMany()).isNotNull();
                assertThat(entityColumnDetails.getManyToMany().getMappedBy()).isEmpty();
                assertThat(entityColumnDetails.getJoinTable()).isNotNull();
                assertThat(entityColumnDetails.getJoinTable().getName()).isEqualTo("books_authors");
                assertThat(entityColumnDetails.getJoinTable().getJoinColumn()).isEqualTo("book_id");
                assertThat(entityColumnDetails.getJoinTable().getInverseJoinColumn()).isEqualTo("author_id");
            }
        }
    }
}