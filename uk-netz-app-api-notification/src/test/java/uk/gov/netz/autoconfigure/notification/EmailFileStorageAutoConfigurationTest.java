package uk.gov.netz.autoconfigure.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.notification.mail.files.EmailLinkProperties;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailFileStorageAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EmailFileStorageAutoConfiguration.class));

    @Test
    void disabledIntegrationWorksWithoutFilesOrAwsLibraries() {
        runner.withClassLoader(new FilteredClassLoader("uk.gov.netz.api.files", "io.awspring", "software.amazon"))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(EmailLinkedFileStorageService.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk.gov.netz.api.files", "uk.gov.netz.api.files.storage"})
    void broadPropertyScanningWorksWhenOptionalStorageTypesAreAbsent(String missingPackage) {
        runner.withClassLoader(new FilteredClassLoader(missingPackage, "io.awspring", "software.amazon"))
                .withUserConfiguration(PropertyScanningConfiguration.class)
                .withPropertyValues("notification.email-link.enabled=false")
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(EmailLinkProperties.class)
                        .doesNotHaveBean(EmailLinkedFileStorageService.class));
    }

    @Test
    void enabledIntegrationRequiresFilesLibrary() {
        runner.withClassLoader(new FilteredClassLoader("uk.gov.netz.api.files"))
                .withPropertyValues("notification.email-link.enabled=true")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .hasRootCauseMessage("notification.email-link requires uk-netz-app-api-files and a FileStorageService bean"));
    }

    @Test
    void enabledIntegrationRequiresStorageBean() {
        runner.withPropertyValues("notification.email-link.enabled=true", "notification.email-link.prefix=app")
                .run(context -> assertThat(context).hasFailed().getFailure().hasMessageContaining("FileStorageService"));
    }

    @Test
    void usesNeutralStorageWithoutAwsLibraries() {
        runner.withClassLoader(new FilteredClassLoader("io.awspring", "software.amazon"))
                .withBean(FileStorageService.class, () -> mock(FileStorageService.class))
                .withPropertyValues("notification.email-link.enabled=true", "notification.email-link.prefix=app")
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(EmailLinkedFileStorageService.class));
    }

    @Test
    void customEmailProviderNeedsNeitherFilesNorDestinationConfiguration() {
        EmailLinkedFileStorageService custom = files -> List.of();
        runner.withClassLoader(new FilteredClassLoader("uk.gov.netz.api.files", "io.awspring", "software.amazon"))
                .withUserConfiguration(PropertyScanningConfiguration.class)
                .withPropertyValues("notification.email-link.enabled=true")
                .withBean(EmailLinkedFileStorageService.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(EmailLinkProperties.class);
                    assertThat(context.getBean(EmailLinkedFileStorageService.class)).isSameAs(custom);
                });
    }

    @Test
    void enabledIntegrationRejectsMissingPrefixAndInvalidBaseUrl() {
        runner.withBean(FileStorageService.class, () -> mock(FileStorageService.class))
                .withPropertyValues("notification.email-link.enabled=true")
                .run(context -> assertThat(context).hasFailed().getFailure().hasRootCauseMessage("storage prefix must not be blank"));
        runner.withBean(FileStorageService.class, () -> mock(FileStorageService.class))
                .withPropertyValues("notification.email-link.enabled=true", "notification.email-link.prefix=app",
                        "notification.email-link.public-base-url=relative")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .hasRootCauseMessage("notification.email-link.public-base-url must be an absolute HTTP(S) URI without user info, query, or fragment"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/base%2Fsegment///", "/my%20files", "/base%25value", "/base%3Fpart%23section"})
    void propertyBindingPreservesEncodedPublicBaseUrl(String basePath) {
        String publicBaseUrl = "https://files.example.gov.uk" + basePath;
        runner.withBean(FileStorageService.class, () -> mock(FileStorageService.class))
                .withPropertyValues("notification.email-link.enabled=true", "notification.email-link.prefix=app",
                        "notification.email-link.public-base-url=" + publicBaseUrl)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(EmailLinkProperties.class).getPublicBaseUrl())
                            .isEqualTo(URI.create(publicBaseUrl));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @ConfigurationPropertiesScan("uk.gov.netz.api.notification")
    static class PropertyScanningConfiguration {
    }
}
