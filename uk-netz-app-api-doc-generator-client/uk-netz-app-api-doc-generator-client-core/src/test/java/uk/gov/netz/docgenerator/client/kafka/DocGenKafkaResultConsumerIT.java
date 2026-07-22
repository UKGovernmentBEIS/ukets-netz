package uk.gov.netz.docgenerator.client.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.netz.docgenerator.client.ConversionResultHandler;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;

@Testcontainers
@DirtiesContext
@SpringBootTest(
    classes = DocGenKafkaResultConsumerIT.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "kafka.docgen-consumer.enabled=true",
        "kafka.docgen-consumer.topic=doc.converted",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=uk.gov.netz.docgenerator.client.model",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer"
    }
)
class DocGenKafkaResultConsumerIT {

    private static final String TOPIC = "doc.converted";
    private static final String GROUP_ID = "docgen-result-consumer-it-" + UUID.randomUUID();

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.7.1")
    );

    @Autowired
    private RecordingHandler recordingHandler;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("kafka.docgen-consumer.group", () -> GROUP_ID);
    }

    @Test
    void brokerBackedConsumerDispatchesEventAndCommitsOffset() throws Exception {
        ConversionEvent event = ConversionEvent.builder()
            .jobId("job-kafka-1")
            .status("COMPLETE")
            .outputS3Key("output/job-kafka-1.pdf")
            .durationMs(125L)
            .build();

        publish(event);

        assertThat(recordingHandler.awaitEvent()).isTrue();
        assertThat(recordingHandler.event.get()).usingRecursiveComparison().isEqualTo(event);
        assertThat(awaitCommittedOffset()).isGreaterThanOrEqualTo(1L);
    }

    private static void publish(ConversionEvent event) throws Exception {
        try (KafkaProducer<String, ConversionEvent> producer = new KafkaProducer<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
            JsonSerializer.ADD_TYPE_INFO_HEADERS, false
        ))) {
            producer.send(new ProducerRecord<>(TOPIC, event.getJobId(), event)).get(30, TimeUnit.SECONDS);
            producer.flush();
        }
    }

    private static long awaitCommittedOffset() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < deadline) {
            OffsetAndMetadata offsetAndMetadata = committedOffset();
            if (offsetAndMetadata != null) {
                return offsetAndMetadata.offset();
            }
            Thread.sleep(200L);
        }
        return -1L;
    }

    private static OffsetAndMetadata committedOffset() {
        TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ))) {
            return consumer.committed(Set.of(topicPartition), Duration.ofSeconds(5)).get(topicPartition);
        }
    }

    static class RecordingHandler implements ConversionResultHandler, Ordered {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<ConversionEvent> event = new AtomicReference<>();

        @Override
        public void handle(ConversionEvent event) {
            this.event.set(event);
            latch.countDown();
        }

        @Override
        public int getOrder() {
            return 10;
        }

        boolean awaitEvent() throws InterruptedException {
            return latch.await(30, TimeUnit.SECONDS);
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        org.apache.kafka.clients.admin.NewTopic docConvertedTopic() {
            return TopicBuilder.name(TOPIC).partitions(1).replicas(1).build();
        }

        @Bean
        RecordingHandler recordingHandler() {
            return new RecordingHandler();
        }
    }
}
