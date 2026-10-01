package uk.gov.netz.api.files.storage.s3;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.s3.S3Operations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.FileUpload;
import uk.gov.netz.api.files.storage.StorageDestination;
import uk.gov.netz.autoconfigure.files.S3FileStorageAutoConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
class S3FileStorageIT {

    @Container
    private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.8.1")).withServices(S3);

    @Test
    void storesAndDeletesAcrossDestinationsUsingOneConfiguredClient() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AwsAutoConfiguration.class, CredentialsProviderAutoConfiguration.class,
                        RegionProviderAutoConfiguration.class, S3AutoConfiguration.class, S3FileStorageAutoConfiguration.class))
                .withPropertyValues(
                        "spring.cloud.aws.endpoint=" + LOCALSTACK.getEndpointOverride(S3),
                        "spring.cloud.aws.credentials.access-key=storage-test",
                        "spring.cloud.aws.credentials.secret-key=storage-secret",
                        "spring.cloud.aws.region.static=" + LOCALSTACK.getRegion(),
                        "spring.cloud.aws.s3.path-style-access-enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(S3Client.class)
                            .hasSingleBean(S3Operations.class).hasSingleBean(FileStorageService.class);
                    S3Operations operations = context.getBean(S3Operations.class);
                    FileStorageService storage = context.getBean(FileStorageService.class);
                    byte[] content = "provider-neutral content".getBytes(StandardCharsets.UTF_8);
                    List<StorageDestination> destinations = List.of(
                            new StorageDestination("first-container", " /reports//annual/ "),
                            new StorageDestination("second-container", "other"));
                    for (StorageDestination destination : destinations) {
                        operations.createBucket(destination.getContainer());
                        var file = storage.storeFiles(destination, List.of(FileUpload.builder()
                                .fileName("report.txt").content(content).contentType("text/plain")
                                .contentDisposition("inline").cacheControl("max-age=60").build())).getFirst();
                        assertThat(file.getReference().getContainer()).isEqualTo(destination.getContainer());
                        assertThat(file.getReference().getPath()).isEqualTo(destination.getPrefix() + "/" + file.getUuid());
                        var stored = context.getBean(S3Client.class).getObjectAsBytes(request ->
                                request.bucket(file.getReference().getContainer()).key(file.getReference().getPath()));
                        assertThat(stored.asByteArray()).isEqualTo(content);
                        assertThat(stored.response().contentLength()).isEqualTo(content.length);
                        assertThat(stored.response().contentType()).isEqualTo("text/plain");
                        assertThat(stored.response().contentDisposition()).isEqualTo("inline");
                        assertThat(stored.response().cacheControl()).isEqualTo("max-age=60");
                        storage.deleteFile(file.getReference());
                        assertThat(operations.objectExists(file.getReference().getContainer(), file.getReference().getPath())).isFalse();
                    }
                });
    }
}
