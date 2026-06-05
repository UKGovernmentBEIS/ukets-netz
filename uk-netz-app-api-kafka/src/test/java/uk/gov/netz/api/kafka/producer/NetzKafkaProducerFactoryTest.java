package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import uk.gov.netz.api.kafka.TestLoggingUtils;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationParentIdResolver;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(kraft = true,
		topics = {
				"test-topic"
		},
		partitions = 1,
		brokerProperties = {
				"transaction.state.log.replication.factor=1",
		}
)
class NetzKafkaProducerFactoryTest {
	private ListAppender listAppender;

	@BeforeEach
	void setUp() {
		LoggerContext loggerContext = LoggerContext.getContext(false);
		Logger logger = loggerContext.getLogger("uk.gov");
		listAppender = (ListAppender) logger.getAppenders().get("listAppender");
	}

	@AfterEach
	void tearDown() {
		listAppender.clear();
	}

	@Test
	void createKafkaTemplate_with_empty_netz_properties(EmbeddedKafkaBroker broker) {
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getProducer().getProperties().put("producerProp1", "producerProp1Value");
		kafkaProperties.getProducer().setClientId("client-id");
		kafkaProperties.getProducer().setTransactionIdPrefix("trans-prefix");
		
		NetzKafkaProducerFactory<String, Object> cut = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());

		KafkaTemplate<String, Object> resultKafkaTemplate = cut.createKafkaTemplate();

		ProducerFactory<String, Object> resultProducerFactory = resultKafkaTemplate.getProducerFactory();

		assertThat(resultProducerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
		assertThat(resultProducerFactory.getConfigurationProperties())
				.containsExactlyInAnyOrderEntriesOf(Map.of(
						ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
						JsonSerializer.ADD_TYPE_INFO_HEADERS, false,
						ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
						ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
						ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, List.of(broker.getBrokersAsString()),
						"producerProp1", "producerProp1Value",
						ProducerConfig.CLIENT_ID_CONFIG, "client-id",
						ProducerConfig.ACKS_CONFIG, "all"
				));

		assertThat(resultProducerFactory.getTransactionIdPrefix()).startsWith("trans-prefix");

		resultKafkaTemplate.executeInTransaction(template -> {
			CompletableFuture<SendResult<String, Object>> result = template.send("test-topic", "key", new Entity("value1"));
			result.whenComplete((sendResult, throwable) -> {
				assertNotNull(sendResult.getProducerRecord().headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER));
				assertNotNull(sendResult.getProducerRecord().headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER));
				assertNotNull(sendResult.getProducerRecord().headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER));
			});
			return true;
		});

		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);

		assertEquals( 1, loggingEntries.size());
		TestLoggingUtils.LogEntry logEntry = loggingEntries.getFirst();
		assertEquals(logEntry.getLogLevel(), Level.INFO);
		KafkaLoggingEntry<String> kafkaLoggingEntry = logEntry.getLogEntry();
		assertEquals(KafkaLoggingEntry.Type.PRODUCING, kafkaLoggingEntry.getType());
		assertNotNull(kafkaLoggingEntry.getCorrelationId());
		assertEquals("client-id", kafkaLoggingEntry.getClientId());
		assertEquals("key", kafkaLoggingEntry.getRecordKey().toString());
		assertEquals(Map.of("payload", Map.of("property1", "value1")), kafkaLoggingEntry.getRecordValue());
		assertEquals("test-topic", kafkaLoggingEntry.getTopic());
		assertEquals(0, kafkaLoggingEntry.getPartition());
		assertNotNull(kafkaLoggingEntry.getOffset());
		assertNotNull(kafkaLoggingEntry.getTimestamp());
	}

	@Test
	void createKafkaTemplate_with_not_empty_netz_properties(EmbeddedKafkaBroker broker) {
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getProducer().setBootstrapServers(List.of("kafkaUrl"));
		kafkaProperties.getProducer().getProperties().put("producerProp1", "producerProp1Value");
		kafkaProperties.getProducer().setClientId("client-id-1");
		kafkaProperties.getProducer().setTransactionIdPrefix("trans-prefix");

		NetzKafkaProducerProperties netzKafkaProperties = new NetzKafkaProducerProperties();
		netzKafkaProperties.setProducer(new KafkaProperties.Producer());
		netzKafkaProperties.getProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		netzKafkaProperties.getProducer().getProperties().put("producerProp2", "producerProp2Value");
		netzKafkaProperties.getProducer().setClientId("client-id-2");
		netzKafkaProperties.setTransactional(true);
		netzKafkaProperties.getProducer().setTransactionIdPrefix("trans-netz-prefix");
		
		NetzKafkaProducerFactory<String, Object> cut = new NetzKafkaProducerFactory<>(kafkaProperties,
				new TestKafkaCorrelationHeaderProducerInterceptor<>(),
				new TestKafkaCorrelationParentHeaderProducerInterceptor<>());

		KafkaTemplate<String, Object> resultKafkaTemplate = cut.createKafkaTemplate(netzKafkaProperties);

		ProducerFactory<String, Object> resultProducerFactory = resultKafkaTemplate.getProducerFactory();

		assertThat(resultProducerFactory).isInstanceOf(DefaultKafkaProducerFactory.class);
		assertThat(resultProducerFactory.getConfigurationProperties())
				.containsExactlyInAnyOrderEntriesOf(Map.of(
						ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
						JsonSerializer.ADD_TYPE_INFO_HEADERS, false,
						ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
						ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
						ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, List.of(broker.getBrokersAsString()),
						"producerProp1", "producerProp1Value",
						"producerProp2", "producerProp2Value",
						ProducerConfig.CLIENT_ID_CONFIG, "client-id-2",
						ProducerConfig.ACKS_CONFIG, "all"
				));

		assertThat(resultProducerFactory.getTransactionIdPrefix()).startsWith("trans-netz-prefix");

		resultKafkaTemplate.executeInTransaction(template -> {
			CompletableFuture<SendResult<String, Object>> result = template.send("test-topic", "key", new Entity("value1"));
			result.whenComplete((result1, throwable) -> {
				assertNotNull(result1.getProducerRecord().headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER));
				assertNotNull(result1.getProducerRecord().headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER));
				assertNotNull(result1.getProducerRecord().headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER));
			});
			return true;
		});

		List<TestLoggingUtils.LogEntry> loggingEntries = TestLoggingUtils.getLoggingEntries(listAppender);

		assertEquals(1, loggingEntries.size());
		TestLoggingUtils.LogEntry logEntry = loggingEntries.getFirst();
		assertEquals(Level.INFO, logEntry.getLogLevel());
		KafkaLoggingEntry<String> kafkaLoggingEntry = logEntry.getLogEntry();
		assertEquals(KafkaLoggingEntry.Type.PRODUCING, kafkaLoggingEntry.getType());
		assertNotNull(kafkaLoggingEntry.getCorrelationId());
		assertEquals("client-id-2", kafkaLoggingEntry.getClientId());
		assertEquals("key", kafkaLoggingEntry.getRecordKey().toString());
		assertEquals(Map.of("payload", Map.of("property1", "value1")), kafkaLoggingEntry.getRecordValue());
		assertEquals("test-topic", kafkaLoggingEntry.getTopic());
		assertEquals(0, kafkaLoggingEntry.getPartition());
		assertNotNull(kafkaLoggingEntry.getOffset());
		assertNotNull(kafkaLoggingEntry.getTimestamp());
	}

	@Test
	void createKafkaTemplate_without_parent_interceptor_or_resolver_uses_default_without_parent_header(
			EmbeddedKafkaBroker broker) {
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getProducer().setClientId("client-id-no-parent");
		kafkaProperties.getProducer().setTransactionIdPrefix("trans-prefix-no-parent");

		NetzKafkaProducerFactory<String, Object> cut = new NetzKafkaProducerFactory<>(
				kafkaProperties,
				new KafkaCorrelationHeaderProducerDefaultInterceptor<>(),
				emptyProvider(),
				emptyProvider());

		KafkaTemplate<String, Object> resultKafkaTemplate = cut.createKafkaTemplate();

		resultKafkaTemplate.executeInTransaction(template -> {
			SendResult<String, Object> sendResult =
					template.send("test-topic", "key-no-parent", new Entity("value1")).join();
			assertNotNull(sendResult.getProducerRecord().headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER));
			assertThat(sendResult.getProducerRecord().headers()
					.lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER)).isNull();
			return true;
		});
	}

	@Test
	void createKafkaTemplate_uses_app_specific_parent_interceptor_when_provided(EmbeddedKafkaBroker broker) {
		KafkaProperties kafkaProperties = new KafkaProperties();
		kafkaProperties.getProducer().setBootstrapServers(List.of(broker.getBrokersAsString()));
		kafkaProperties.getProducer().setClientId("client-id-app-parent");
		kafkaProperties.getProducer().setTransactionIdPrefix("trans-prefix-app-parent");

		NetzKafkaProducerFactory<String, Object> cut = new NetzKafkaProducerFactory<>(
				kafkaProperties,
				new KafkaCorrelationHeaderProducerDefaultInterceptor<>(),
				providerOf(new StaticParentHeaderInterceptor<>("app-parent")),
				providerOf((KafkaCorrelationParentIdResolver) () -> "resolver-parent"));

		KafkaTemplate<String, Object> resultKafkaTemplate = cut.createKafkaTemplate();

		resultKafkaTemplate.executeInTransaction(template -> {
			SendResult<String, Object> sendResult =
					template.send("test-topic", "key-app-parent", new Entity("value1")).join();
			assertThat(parentHeader(sendResult.getProducerRecord())).isEqualTo("app-parent");
			return true;
		});
	}
	
	private static String parentHeader(ProducerRecord<?, ?> record) {
		var header = record.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER);
		return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
	}

	private static <T> ObjectProvider<T> emptyProvider() {
		return providerOf(null);
	}

	private static <T> ObjectProvider<T> providerOf(T value) {
		@SuppressWarnings("unchecked")
		ObjectProvider<T> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(value);
		when(provider.getIfAvailable(any(Supplier.class))).thenAnswer(invocation -> {
			Supplier<T> defaultSupplier = invocation.getArgument(0);
			return value != null ? value : defaultSupplier.get();
		});
		when(provider.orderedStream()).thenReturn(value != null ? Stream.of(value) : Stream.empty());
		return provider;
	}

	private static class StaticParentHeaderInterceptor<K, V>
			implements KafkaCorrelationParentHeaderProducerInterceptor<K, V> {

		private final String parentCorrelationId;

		private StaticParentHeaderInterceptor(String parentCorrelationId) {
			this.parentCorrelationId = parentCorrelationId;
		}

		@Override
		public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
			record.headers().add(KafkaConstants.CORRELATION_PARENT_ID_HEADER,
					parentCorrelationId.getBytes(StandardCharsets.UTF_8));
			return record;
		}

		@Override
		public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
		}

		@Override
		public void close() {
		}

		@Override
		public void configure(Map<String, ?> configs) {
		}
	}

	record Entity(String property1) { }
}

