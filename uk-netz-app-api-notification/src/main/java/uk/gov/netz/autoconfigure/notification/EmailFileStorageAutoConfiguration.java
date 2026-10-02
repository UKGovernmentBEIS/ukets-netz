package uk.gov.netz.autoconfigure.notification;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.notification.mail.files.EmailLinkProperties;
import uk.gov.netz.api.notification.mail.files.StoredEmailLinkedFileStorageService;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;

@AutoConfiguration
@ConditionalOnProperty(prefix = "notification.email-link", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(EmailLinkedFileStorageService.class)
public class EmailFileStorageAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "uk.gov.netz.api.files.storage.FileStorageService")
    @EnableConfigurationProperties(EmailLinkProperties.class)
    static class StorageAvailableConfiguration {

        @Bean
        StoredEmailLinkedFileStorageService emailLinkedFileStorageService(FileStorageService storage, EmailLinkProperties properties) {
            return new StoredEmailLinkedFileStorageService(storage, properties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass("uk.gov.netz.api.files.storage.FileStorageService")
    static class StorageMissingConfiguration {

        @Bean
        EmailLinkedFileStorageService emailLinkedFileStorageService() {
            throw new IllegalStateException(
                    "notification.email-link requires uk-netz-app-api-files and a FileStorageService bean");
        }
    }
}
