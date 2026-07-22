package uk.gov.netz.docgenerator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import uk.gov.netz.api.kafka.consumer.NetzKafkaConsumerFactory;
import uk.gov.netz.api.kafka.producer.KafkaCorrelationHeaderProducerDefaultInterceptor;
import uk.gov.netz.api.kafka.producer.NetzKafkaProducerFactory;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaCorrelationParentHeaderProducerInterceptor;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaConsumerConfigProperties;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaPoisonEventTracker;
import uk.gov.netz.docgenerator.client.kafka.DocGenKafkaResultConsumer;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;

class DocGenClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DocGenClientAutoConfiguration.class));

    @Test
    void facadeBeanIsCreatedWhenStorageAndQueueAdaptersArePresent() {
        contextRunner
            .withBean(DocumentStorageClient.class, () -> mock(DocumentStorageClient.class))
            .withBean(JobQueuePublisher.class, () -> mock(JobQueuePublisher.class))
            .run(context -> {
                assertThat(context).hasSingleBean(DocumentGeneratorClient.class);
            });
    }

    @Test
    void docGenClientPropertiesDefaultToStandardDocumentGeneratorContract() {
        contextRunner.run(context -> {
            DocGenClientProperties properties = context.getBean(DocGenClientProperties.class);

            assertThat(properties.getStorageContainer()).isEqualTo("doc-gen");
            assertThat(properties.getSyncQueueDestination()).isEqualTo("doc-gen-sync-jobs");
            assertThat(properties.getAsyncQueueDestination()).isEqualTo("doc-gen-async-jobs");
        });
    }

    @Test
    void facadeIsNotCreatedWithoutRequiredGenericAdapters() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DocumentGeneratorClient.class);
        });
    }

    @Test
    void consumingApplicationsCanOverrideFacade() {
        DocumentGeneratorClient customClient = mock(DocumentGeneratorClient.class);

        contextRunner
            .withBean(DocumentStorageClient.class, () -> mock(DocumentStorageClient.class))
            .withBean(JobQueuePublisher.class, () -> mock(JobQueuePublisher.class))
            .withBean(DocumentGeneratorClient.class, () -> customClient)
            .run(context -> {
                assertThat(context).hasSingleBean(DocumentGeneratorClient.class);
                assertThat(context).getBean(DocumentGeneratorClient.class).isSameAs(customClient);
            });
    }

    @Test
    void kafkaBeansAreNotCreatedByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DocGenKafkaPoisonEventTracker.class);
            assertThat(context).doesNotHaveBean(DocGenKafkaResultConsumer.class);
            assertThat(context).doesNotHaveBean("docGenKafkaListenerContainerFactory");
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void docGenKafkaListenerContainerFactoryUsesNetzFactoryAndManualSynchronousCommits() {
        DocGenClientAutoConfiguration.KafkaAutoConfiguration autoConfiguration =
            new DocGenClientAutoConfiguration.KafkaAutoConfiguration();
        DocGenKafkaConsumerConfigProperties properties = new DocGenKafkaConsumerConfigProperties();
        properties.setGroup("docgen-group");
        properties.setConsumer(new KafkaProperties.Consumer());
        properties.getConsumer().setEnableAutoCommit(true);
        ConcurrentKafkaListenerContainerFactory<String, ConversionEvent> delegatedFactory =
            new ConcurrentKafkaListenerContainerFactory<>();
        NetzKafkaConsumerFactory<String, ConversionEvent> netzKafkaConsumerFactory = mock(NetzKafkaConsumerFactory.class);
        when(netzKafkaConsumerFactory.createKafkaListenerContainerFactory(
            eq("docgen-group"),
            same(properties),
            eq(ConversionEvent.class)
        )).thenReturn(delegatedFactory);

        ConcurrentKafkaListenerContainerFactory<String, ConversionEvent> factory =
            autoConfiguration.docGenKafkaListenerContainerFactory(netzKafkaConsumerFactory, properties);

        assertThat(factory).isSameAs(delegatedFactory);
        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        assertThat(factory.getContainerProperties().isSyncCommits()).isTrue();
        assertThat(properties.getConsumer().getEnableAutoCommit()).isFalse();
        verify(netzKafkaConsumerFactory).createKafkaListenerContainerFactory(
            "docgen-group",
            properties,
            ConversionEvent.class
        );
    }

    @Test
    void docGenKafkaListenerContainerFactoryDoesNotMutateGlobalKafkaConsumerProperties() {
        DocGenClientAutoConfiguration.KafkaAutoConfiguration autoConfiguration =
            new DocGenClientAutoConfiguration.KafkaAutoConfiguration();
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.getConsumer().setEnableAutoCommit(true);
        kafkaProperties.getConsumer().setGroupId("global-group");
        DocGenKafkaConsumerConfigProperties properties = new DocGenKafkaConsumerConfigProperties();
        properties.setGroup("docgen-group");
        NetzKafkaProducerFactory<String, ConversionEvent> producerFactory = new NetzKafkaProducerFactory<>(
            kafkaProperties,
            new KafkaCorrelationHeaderProducerDefaultInterceptor<>(),
            new DocGenKafkaCorrelationParentHeaderProducerInterceptor<>()
        );
        NetzKafkaConsumerFactory<String, ConversionEvent> consumerFactory =
            new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);

        ConcurrentKafkaListenerContainerFactory<String, ConversionEvent> factory =
            autoConfiguration.docGenKafkaListenerContainerFactory(consumerFactory, properties);

        assertThat(factory.getConsumerFactory().getConfigurationProperties())
            .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        assertThat(kafkaProperties.getConsumer().getEnableAutoCommit()).isTrue();
        assertThat(kafkaProperties.getConsumer().getGroupId()).isEqualTo("global-group");
    }
}
