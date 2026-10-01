package uk.gov.netz.api.files.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageDestinationTest {

    @ParameterizedTest
    @ValueSource(strings = {
        " / reports // annual / ", "\u2003reports\u2003/\u2003annual\u2003",
        "\u3000/reports/\u2003/annual/\u2002", "\0reports\0/\0annual\0",
        "\u2003\0 reports \0\u2003/\0\u2003annual\u2003\0"
    })
    void normalizesPrefix(String prefix) {
        StorageDestination constructed = new StorageDestination("documents", prefix);
        StorageDestination built = StorageDestination.builder().container("documents").prefix(prefix).build();
        assertThat(constructed.getPrefix()).isEqualTo("reports/annual");
        assertThat(built).isEqualTo(constructed);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "/", "///", "a/../b", "./b", "\u2003", "\u2003/\u3000/\u2002",
        "a/\u2003..\u2003/b", "\u2003.\u2003/b", "\u2003\0\u2003", "a/\u2003\0..\0\u2003/b"
    })
    void rejectsInvalidPrefixes(String prefix) {
        assertThatThrownBy(() -> new StorageDestination("documents", prefix)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StorageDestination.builder().container("documents").prefix(prefix).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsBlankContainers(String container) {
        assertThatThrownBy(() -> new StorageDestination(container, "reports")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StorageDestination.builder().container(container).prefix("reports").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void preservesInternalWhitespaceAndUnicodeCharacters() {
        String prefix = "船舶-🚢/annual\u2003reports";
        assertThat(new StorageDestination("documents", prefix).getPrefix()).isEqualTo(prefix);
    }

    @Test
    void buildersPreserveStorageMetadata() {
        UUID uuid = UUID.randomUUID();
        FileReference reference = new FileReference("documents", "reports/" + uuid);
        StoredFile stored = new StoredFile(uuid, reference, "report.txt", "text/plain", 6);
        FileReference.FileReferenceBuilder referenceBuilder = FileReference.builder().container("documents").path(reference.getPath());
        assertThat(referenceBuilder.build()).isEqualTo(reference);
        StoredFile.StoredFileBuilder storedBuilder = StoredFile.builder().uuid(uuid).reference(reference)
                .fileName("report.txt").contentType("text/plain").size(6);
        assertThat(storedBuilder.build()).isEqualTo(stored);
    }
}
