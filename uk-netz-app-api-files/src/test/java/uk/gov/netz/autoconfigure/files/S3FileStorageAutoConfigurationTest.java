package uk.gov.netz.autoconfigure.files;

import io.awspring.cloud.s3.S3Operations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.netz.api.files.storage.FileStorageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class S3FileStorageAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(S3FileStorageAutoConfiguration.class));

    @Test
    void requiresAnExistingOperationsBean() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(FileStorageService.class));
    }

    @Test
    void usesExistingOperations() {
        runner.withBean(S3Operations.class, () -> mock(S3Operations.class))
                .run(context -> assertThat(context).hasNotFailed()
                        .hasSingleBean(FileStorageService.class).hasSingleBean(S3Operations.class));
    }

    @Test
    void backsOffForCustomStorage() {
        FileStorageService storage = mock(FileStorageService.class);
        runner.withBean(FileStorageService.class, () -> storage)
                .withBean(S3Operations.class, () -> mock(S3Operations.class))
                .run(context -> assertThat(context.getBean(FileStorageService.class)).isSameAs(storage));
    }

    @Test
    void loadsWithoutAwsLibraries() {
        runner.withClassLoader(new FilteredClassLoader("io.awspring", "software.amazon"))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(FileStorageService.class));
    }

    @Test
    void customProviderWorksWithoutAwsLibraries() {
        FileStorageService storage = mock(FileStorageService.class);
        runner.withClassLoader(new FilteredClassLoader("io.awspring", "software.amazon"))
                .withBean(FileStorageService.class, () -> storage)
                .run(context -> assertThat(context).hasNotFailed().getBean(FileStorageService.class).isSameAs(storage));
    }
}
