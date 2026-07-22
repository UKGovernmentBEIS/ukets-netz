package uk.gov.netz.docgenerator.client;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import uk.gov.netz.api.kafka.consumer.NetzKafkaConsumerFactory;
import uk.gov.netz.api.kafka.producer.KafkaCorrelationHeaderProducerDefaultInterceptor;
import uk.gov.netz.api.kafka.producer.KafkaCorrelationHeaderProducerInterceptor;
import uk.gov.netz.api.kafka.producer.KafkaCorrelationParentHeaderProducerInterceptor;
import uk.gov.netz.api.kafka.producer.NetzKafkaProducerFactory;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaConsumerConfigProperties;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaCorrelationParentHeaderProducerInterceptor;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaPoisonEventTracker;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaResultConsumer;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;

@AutoConfiguration
@EnableConfigurationProperties({
    DocGenClientProperties.class,
    DocGenKafkaConsumerConfigProperties.class,
    KafkaProperties.class
})
public class DocGenClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({DocumentStorageClient.class, JobQueuePublisher.class})
    public DocumentGeneratorClient docGenClient(
        DocumentStorageClient documentStorageClient,
        JobQueuePublisher jobQueuePublisher,
        DocGenClientProperties properties
    ) {
        return new DocumentGeneratorClient(documentStorageClient, jobQueuePublisher, properties);
    }

    @EnableKafka
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({KafkaListener.class, NetzKafkaConsumerFactory.class})
    static class KafkaAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(KafkaCorrelationHeaderProducerInterceptor.class)
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public KafkaCorrelationHeaderProducerInterceptor<String, ConversionEvent> docGenKafkaCorrelationHeaderProducerInterceptor() {
            return new KafkaCorrelationHeaderProducerDefaultInterceptor<>();
        }

        @Bean
        @ConditionalOnMissingBean(KafkaCorrelationParentHeaderProducerInterceptor.class)
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public KafkaCorrelationParentHeaderProducerInterceptor<String, ConversionEvent> docGenKafkaCorrelationParentHeaderProducerInterceptor() {
            return new DocGenKafkaCorrelationParentHeaderProducerInterceptor<>();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public NetzKafkaProducerFactory<String, ConversionEvent> docGenKafkaProducerFactory(
            KafkaProperties kafkaProperties,
            KafkaCorrelationHeaderProducerInterceptor<String, ConversionEvent> correlationHeaderInterceptor,
            KafkaCorrelationParentHeaderProducerInterceptor<String, ConversionEvent> correlationParentHeaderInterceptor
        ) {
            return new NetzKafkaProducerFactory<>(
                kafkaProperties,
                correlationHeaderInterceptor,
                correlationParentHeaderInterceptor
            );
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public NetzKafkaConsumerFactory<String, ConversionEvent> docGenNetzKafkaConsumerFactory(
            KafkaProperties kafkaProperties,
            NetzKafkaProducerFactory<String, ConversionEvent> netzKafkaProducerFactory
        ) {
            return new NetzKafkaConsumerFactory<>(kafkaProperties, netzKafkaProducerFactory);
        }

        @Bean(name = "docGenKafkaListenerContainerFactory")
        @ConditionalOnMissingBean(name = "docGenKafkaListenerContainerFactory")
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public ConcurrentKafkaListenerContainerFactory<String, ConversionEvent> docGenKafkaListenerContainerFactory(
            NetzKafkaConsumerFactory<String, ConversionEvent> netzKafkaConsumerFactory,
            DocGenKafkaConsumerConfigProperties properties
        ) {
            enforceManualOffsetCommits(properties);
            ConcurrentKafkaListenerContainerFactory<String, ConversionEvent> factory =
                netzKafkaConsumerFactory.createKafkaListenerContainerFactory(
                    properties.getGroup(),
                    properties,
                    ConversionEvent.class
                );
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
            factory.getContainerProperties().setSyncCommits(true);
            factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(0L, Long.MAX_VALUE)));
            return factory;
        }

        private static void enforceManualOffsetCommits(DocGenKafkaConsumerConfigProperties properties) {
            KafkaProperties.Consumer consumerProperties = properties.getConsumer();
            if (consumerProperties == null) {
                consumerProperties = new KafkaProperties.Consumer();
                properties.setConsumer(consumerProperties);
            }
            consumerProperties.setEnableAutoCommit(false);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public DocGenKafkaPoisonEventTracker docGenKafkaPoisonEventTracker() {
            return new DocGenKafkaPoisonEventTracker();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "kafka.docgen-consumer", name = "enabled", havingValue = "true")
        public DocGenKafkaResultConsumer docGenKafkaResultConsumer(
            List<ConversionResultHandler> conversionResultHandlers,
            DocGenKafkaPoisonEventTracker docGenKafkaPoisonEventTracker,
            DocGenClientProperties properties
        ) {
            return new DocGenKafkaResultConsumer(conversionResultHandlers, docGenKafkaPoisonEventTracker, properties);
        }
    }
}
