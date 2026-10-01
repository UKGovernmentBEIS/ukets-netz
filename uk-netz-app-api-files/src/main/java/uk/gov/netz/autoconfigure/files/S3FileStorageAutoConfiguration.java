package uk.gov.netz.autoconfigure.files;

import io.awspring.cloud.s3.S3Operations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.s3.S3FileStorageService;

@AutoConfiguration(afterName = "io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration")
@ConditionalOnClass(S3Operations.class)
@ConditionalOnBean(S3Operations.class)
public class S3FileStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    public S3FileStorageService fileStorageService(S3Operations operations) {
        return new S3FileStorageService(operations);
    }
}
