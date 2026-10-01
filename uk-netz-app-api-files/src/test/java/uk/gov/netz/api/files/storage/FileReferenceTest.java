package uk.gov.netz.api.files.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileReferenceTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validatesContainerForBothConstructionPaths(String container) {
        assertThatThrownBy(() -> new FileReference(container, "reports/file")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FileReference.builder().container(container).path("reports/file").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validatesPathForBothConstructionPaths(String path) {
        assertThatThrownBy(() -> new FileReference("documents", path)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FileReference.builder().container("documents").path(path).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builderPreservesExactPath() {
        FileReference reference = FileReference.builder().container("documents").path("/exact//path/").build();
        assertThat(reference.getPath()).isEqualTo("/exact//path/");
        assertThat(reference).isEqualTo(new FileReference("documents", "/exact//path/"));
    }
}
