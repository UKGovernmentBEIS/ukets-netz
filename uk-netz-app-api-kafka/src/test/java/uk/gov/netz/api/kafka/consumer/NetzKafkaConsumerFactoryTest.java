package uk.gov.netz.api.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import uk.gov.netz.api.kafka.TestLoggingUtils;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry;
import uk.gov.netz.api.kafka.producer.NetzKafkaProducerFactory;
import uk.gov.netz.api.kafka.producer.TestKafkaCorrelationHeaderProducerInterceptor;
import uk.gov.netz.api.kafka.producer.TestKafkaCorrelationParentHeaderProducerInterceptor;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetzKafkaConsumerFactoryTest {
	private final String topic = "test-topic";
	private EmbeddedKafkaBroker broker;
	private ListAppender listAppender;

	@BeforeEach
	void setUp() {
		broker = new EmbeddedKafkaKraftBroker(1,  1, topic);
		broker.afterPropertiesSet();
		LoggerContext loggerContext = LoggerContext.getContext(false);
		Logger logger = loggerContext.getLogger("uk.gov");
		listAppender = (ListAppender) logger.getAppenders().get("listAppender");
	}

	@AfterEach
	void tearDown() {
		broker.destroy();
		listAppender.clear();
	}

	@Test
	void createKafkaListenerContainerFactory_with_empty_groupId() {
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getConsumer().setBootstrapServers(List.of("kafkaUrl"));
		kafkaProperties.getConsumer().getProperties().put("consumerProp1", "consumerProp1Value");
		
		NetzKafkaProducerFactory<String, Object> producerFactory = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());
		
		NetzKafkaConsumerFactory<String, Object> cut = new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);

		assertThrows(NullPointerException.class,
				() -> cut.createKafkaListenerContainerFactory(null, Object.class));
	}

	@Test
	void createKafkaListenerContainerFactory_with_empty_netz_properties() {
		String groupId = "groupId";
		Class<Object> valueType = Object.class;

		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getConsumer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getConsumer().getProperties().put("consumerProp1", "consumerProp1Value");
		kafkaProperties.getConsumer().getProperties().put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		kafkaProperties.getConsumer().setAutoOffsetReset("earliest");

		NetzKafkaProducerFactory<String, Object> producerFactory = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());
		
		NetzKafkaConsumerFactory<String, Object> cut = new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);
		
		ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory = cut
				.createKafkaListenerContainerFactory(groupId, valueType);
		assertTrue(kafkaListenerContainerFactory.getContainerProperties().isMissingTopicsFatal());

		ConsumerFactory<? super String, ? super Object> consumerFactory = kafkaListenerContainerFactory.getConsumerFactory();
		assertThat(consumerFactory).isInstanceOf(DefaultKafkaConsumerFactory.class);

		Map<String, Object> expectedProperties = new HashMap<>();
		expectedProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		expectedProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		expectedProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		expectedProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
		expectedProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType);
		expectedProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, List.of(broker.getBrokersAsString()));
		expectedProperties.put("consumerProp1", "consumerProp1Value");
		expectedProperties.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, true);
		expectedProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
		expectedProperties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, NetzLoggingConsumerInterceptor.class.getName());
		expectedProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		expectedProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		assertThat(consumerFactory.getConfigurationProperties()).containsExactlyInAnyOrderEntriesOf(expectedProperties);

		//produce message
		sendMessage(broker, topic, "key", new Entity("value1"));

		//consume message
		try (Consumer<? super String, ? super Object> consumer = consumerFactory.createConsumer()) {
			consumer.subscribe(List.of("test-topic"));
			ConsumerRecord<? super String, ? super Object> message = KafkaTestUtils.getSingleRecord(consumer, topic);
			assertArrayEquals("client-id".getBytes(), message.headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER).value());
			assertArrayEquals("correlation-id".getBytes(), message.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER).value());
			assertEquals(new Entity("value1"), message.value());
		}

		//validate logs
		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);
		assertEquals(loggingEntries.size(), 1);
		validateLogMessage(loggingEntries.getFirst(), Level.INFO, KafkaLoggingEntry.Type.CONSUMING, topic);
	}

	@Test
	void createKafkaListenerContainerFactory_with_non_empty_netz_properties() {
		String groupId = "groupId";
		Class<Object> valueType = Object.class;

		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getConsumer().setBootstrapServers(List.of("kafkaUrl"));
		kafkaProperties.getConsumer().getProperties().put("consumerProp1", "consumerProp1Value");
		kafkaProperties.getConsumer().getProperties().put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		kafkaProperties.getConsumer().setAutoOffsetReset("earliest");

		NetzKafkaConsumerProperties netzKafkaProperties = new NetzKafkaConsumerProperties();
		netzKafkaProperties.setConsumer(new KafkaProperties.Consumer());
		netzKafkaProperties.setRetryInterval(100L);
		netzKafkaProperties.getConsumer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		netzKafkaProperties.getConsumer().getProperties().put("consumerProp2", "consumerProp2Value");
		
		NetzKafkaProducerFactory<String, Object> producerFactory = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());
		
		NetzKafkaConsumerFactory<String, Object> cut = new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);

		ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory = cut
				.createKafkaListenerContainerFactory(groupId, netzKafkaProperties, valueType);
		assertTrue(kafkaListenerContainerFactory.getContainerProperties().isMissingTopicsFatal());

		ConsumerFactory<? super String, Object> consumerFactory = kafkaListenerContainerFactory.getConsumerFactory();
		assertThat(consumerFactory).isInstanceOf(DefaultKafkaConsumerFactory.class);

		Map<String, Object> expectedProperties = new HashMap<>();
		expectedProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		expectedProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		expectedProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		expectedProperties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
		expectedProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType);
		expectedProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, List.of(broker.getBrokersAsString()));
		expectedProperties.put("consumerProp1", "consumerProp1Value");
		expectedProperties.put("consumerProp2", "consumerProp2Value");
		expectedProperties.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, true);
		expectedProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
		expectedProperties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, NetzLoggingConsumerInterceptor.class.getName());
		expectedProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		expectedProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		assertThat(consumerFactory.getConfigurationProperties()).containsExactlyInAnyOrderEntriesOf(expectedProperties);

		//produce message
		sendMessage(broker, topic, "key", new Entity("value1"));

		//consume message
		try (Consumer<? super String, ? super Object> consumer = consumerFactory.createConsumer()) {
			consumer.subscribe(List.of("test-topic"));
			ConsumerRecord<? super String, ? super Object> message = KafkaTestUtils.getSingleRecord(consumer, topic);
			assertArrayEquals("client-id".getBytes(), message.headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER).value());
			assertArrayEquals("correlation-id".getBytes(), message.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER).value());
			assertEquals(new Entity("value1"), message.value());
		}

		//validate logs
		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);
		assertEquals(loggingEntries.size(), 1);
		validateLogMessage(loggingEntries.getFirst(), Level.INFO, KafkaLoggingEntry.Type.CONSUMING, topic);
	}

	@Test
	void createKafkaListenerContainerFactory_with_empty_netz_properties_error() {
		CountDownLatch stopLatch = new CountDownLatch(2);
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getConsumer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getConsumer().getProperties().put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		kafkaProperties.getConsumer().setAutoOffsetReset("earliest");
		
		NetzKafkaProducerFactory<String, Object> producerFactory = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());
		
		NetzKafkaConsumerFactory<String, Object> cut = new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);

		//consume message and throw exception
		ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory = cut
				.createKafkaListenerContainerFactory("test-group", Object.class);
		ConcurrentMessageListenerContainer<String, Object> container = kafkaListenerContainerFactory.createContainer(topic);
		container.setupMessageListener((MessageListener<String, Object>) record -> {
			stopLatch.countDown();
			throw new RuntimeException("error");
		});
		container.start();
		ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic());

		//consume message from DLQ
		KafkaMessageListenerContainer<Integer, String> deadLetterContainer = getDeadLetterContainer(kafkaProperties.getConsumer());
		deadLetterContainer.setupMessageListener((MessageListener<String, Object>) record -> {
			assertArrayEquals("client-id".getBytes(), record.headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER).value());
			assertArrayEquals("correlation-id".getBytes(), record.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER).value());
			assertArrayEquals("correlation-parent-id".getBytes(), record.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER).value());
			assertEquals(Map.of("property1", "value1"), record.value());
			stopLatch.countDown();
		});
		deadLetterContainer.start();
		ContainerTestUtils.waitForAssignment(deadLetterContainer, broker.getPartitionsPerTopic());

		//produce message
		sendMessage(broker, topic, "key", new Entity("value1"));

		await(stopLatch);

		//validate logs
		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);
		assertEquals(2, loggingEntries.size());
		validateLogMessage(loggingEntries.getFirst(), Level.INFO, KafkaLoggingEntry.Type.CONSUMING, topic);
		validateLogMessage(loggingEntries.get(1), Level.INFO, KafkaLoggingEntry.Type.PRODUCING, topic + "-dlt");
	}

	@Test
	void createKafkaListenerContainerFactory_with_non_empty_netz_properties_error() {
		CountDownLatch stopLatch = new CountDownLatch(2);
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.setBootstrapServers(List.of("brokerUrl"));
		kafkaProperties.getConsumer().setBootstrapServers(List.of("brokerUrl"));
		kafkaProperties.getConsumer().getProperties().put(JsonDeserializer.TRUSTED_PACKAGES, "uk.gov.netz.api.kafka.consumer");
		kafkaProperties.getConsumer().setAutoOffsetReset("latest");

		NetzKafkaConsumerProperties netzKafkaProperties = new NetzKafkaConsumerProperties();
		netzKafkaProperties.setConsumer(new KafkaProperties.Consumer());
		netzKafkaProperties.setDlqProducer(new KafkaProperties.Producer());
		netzKafkaProperties.setRetryInterval(100L);
		netzKafkaProperties.getConsumer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		netzKafkaProperties.getDlqProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		netzKafkaProperties.getConsumer().setAutoOffsetReset("earliest");
		netzKafkaProperties.getDlqProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		
		NetzKafkaProducerFactory<String, Object> producerFactory = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());
		
		NetzKafkaConsumerFactory<String, Object> cut = new NetzKafkaConsumerFactory<>(kafkaProperties, producerFactory);

		//consume message and throw exception
		ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory = cut
				.createKafkaListenerContainerFactory("test-group", netzKafkaProperties, Object.class);
		ConcurrentMessageListenerContainer<String, Object> container = kafkaListenerContainerFactory.createContainer(topic);
		container.setupMessageListener((MessageListener<String, Object>) record -> {
			stopLatch.countDown();
			throw new RuntimeException("error");
		});
		container.start();
		ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic());

		//consume message from DLQ
		KafkaMessageListenerContainer<Integer, String> deadLetterContainer = getDeadLetterContainer(netzKafkaProperties.getConsumer());
		deadLetterContainer.setupMessageListener((MessageListener<String, Object>) record -> {
			assertArrayEquals("client-id".getBytes(), record.headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER).value());
			assertArrayEquals("correlation-id".getBytes(), record.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER).value());
			assertArrayEquals("correlation-parent-id".getBytes(), record.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER).value());
			assertEquals(Map.of("property1", "value1"), record.value());
			stopLatch.countDown();
		});
		deadLetterContainer.start();
		ContainerTestUtils.waitForAssignment(deadLetterContainer, broker.getPartitionsPerTopic());

		//produce message
		sendMessage(broker, topic, "key", new Entity("value1"));

		await(stopLatch);

		//validate logs
		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);
		assertEquals(2, loggingEntries.size());
		validateLogMessage(loggingEntries.getFirst(), Level.INFO, KafkaLoggingEntry.Type.CONSUMING, topic);
		validateLogMessage(loggingEntries.get(1), Level.INFO, KafkaLoggingEntry.Type.PRODUCING, topic + "-dlt");
	}

	private static void sendMessage(EmbeddedKafkaBroker broker, String topic, String key, Object value) {
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(broker);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
		KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);

		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, key, value);
		producerRecord.headers().add(KafkaConstants.PRODUCER_CLIENT_ID_HEADER, "client-id".getBytes());
		producerRecord.headers().add(KafkaConstants.CORRELATION_ID_HEADER, "correlation-id".getBytes());
		producerRecord.headers().add(KafkaConstants.CORRELATION_PARENT_ID_HEADER, "correlation-parent-id".getBytes());

		kafkaTemplate.send(producerRecord);
	}

	private @NotNull KafkaMessageListenerContainer<Integer, String> getDeadLetterContainer(KafkaProperties.Consumer consumer) {
		Map<String, Object> consumerProperties = consumer.buildProperties(null);
		consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-dlq-group");
		consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
		consumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
		consumerProperties.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, true);
		consumerProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
		DefaultKafkaConsumerFactory<Integer, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);

		ContainerProperties containerProperties = new ContainerProperties(topic + "-dlt");
		return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
	}

	private void validateLogMessage(TestLoggingUtils.LogEntry logEntry, Level expectedLogLevel, KafkaLoggingEntry.Type expectedType, String topic) {
		assertEquals(expectedLogLevel, logEntry.getLogLevel());
		KafkaLoggingEntry kafkaLoggingEntry1 = logEntry.getLogEntry();
		assertEquals(expectedType, kafkaLoggingEntry1.getType());
		assertNotNull(kafkaLoggingEntry1.getCorrelationId());
		assertEquals("client-id", kafkaLoggingEntry1.getClientId());
		assertEquals("key", kafkaLoggingEntry1.getRecordKey().toString());
		assertEquals(Map.of("payload", Map.of("property1", "value1")), kafkaLoggingEntry1.getRecordValue());
		assertEquals(topic, kafkaLoggingEntry1.getTopic());
		assertEquals(0, kafkaLoggingEntry1.getPartition());
		assertNotNull(kafkaLoggingEntry1.getOffset());
		assertNotNull(kafkaLoggingEntry1.getTimestamp());
	}

	private void await(CountDownLatch latch) {
		try {
			latch.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	record Entity(String property1) { }
}
