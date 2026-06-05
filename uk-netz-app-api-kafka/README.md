# UK NETZ API KAFKA


## Producer

### Default kafka configuration settings

Spring Kafka provides a list of configuration properties that will be the default properties if not overridden by topic/service specific custom configuration properties (subclasses of NetzKafkaProducerProperties). For example to define a list of default kafka properties that will be applied to all kafka services, the following may be set:

    spring.kafka.bootstrap-servers=localhost:9092
    spring.kafka.producer.client-id=netz-producer

### Required kafka configuration properties:

- The topic name (e.g. kafka.account-updated.topic=account-updated-topic). Should be injected in consumers in order to send the message to the specified topic

### How to override default kafka configuration properties

To define custom configuration properties per producer/consumer service, there exist two basic classes: NetzKafkaProducerProperties and NetzKafkaConsumerProperties , that should be extended by concrete classes.

NetzKafkaProducerProperties:

    producer // By setting any of the producer's properties we can override the default producer settings. 
    transactional // we can set whether the producer is transactional or not

Example how to define a concrete custom configuration class for a producer:

Configuration class:

    @ConfigurationProperties(prefix = "kafka.account-updated-producer")
    @Getter
    @Setter
    public class AccountUpdatedProducerConfigProperties extends NetzKafkaProducerProperties {}

Configuration application properties example that overrides the default kafka properties:

    kafka.account-updated-producer.producer.bootstrap-servers=localhost:9093
    kafka.account-updated-producer.producer.properties.delivery.timeout.ms=60000

### Producer properties that are enforced

    - JsonSerializer.ADD_TYPE_INFO_HEADERS = false
    - ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG = JsonSerializer.class
    - ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG = true
    - ProducerConfig.ACKS_CONFIG = "all"
    
### Correlation id logging

Two interceptor interfaces exist that are responsible for setting the correlation id into the header of the message:

    KafkaCorrelationHeaderProducerInterceptor // responsible for setting the correlation id. 

Default implementation: KafkaCorrelationHeaderProducerDefaultInterceptor: preserves an existing correlation id, otherwise uses the listener-scoped KafkaCorrelationContext correlation id if present, otherwise generates and sets a new random UUID.

    KafkaCorrelationParentHeaderProducerInterceptor // responsible for setting the parent correlation id.

Default implementation: KafkaCorrelationParentHeaderProducerDefaultInterceptor:

- preserves an existing parent correlation id;
- otherwise uses any application-provided KafkaCorrelationParentIdResolver (for example an HTTP response Correlation-Id resolver);
- otherwise uses the listener-scoped KafkaCorrelationContext parent correlation id if present;
- otherwise adds no parent correlation id header.

KafkaCorrelationParentIdResolver is optional. Applications that already provide their own KafkaCorrelationParentHeaderProducerInterceptor continue to use it. Applications that do not provide a resolver and do not seed KafkaCorrelationContext keep the backward-compatible behavior: no Correlation-Parent-Id header is added by the default interceptor.

KafkaCorrelationContext is also optional. It is intended for listener-scoped flows where a consumer handles one Kafka message and synchronously produces one or more related Kafka messages on the same thread. Callers that seed this context must clear it in a finally block or use KafkaCorrelationContextScope with try-with-resources.

## Consumer

### Default kafka configuration settings

Spring Kafka provides a list of configuration properties that will be the default properties if not overridden by topic/service specific custom configuration properties (subclasses of NetzKafkaConsumerProperties). For example to define a list of default kafka properties that will be applied to all kafka services, the following may be set:

    spring.kafka.bootstrap-servers=localhost:9092
    spring.kafka.consumer.client-id=netz-consumer

### Required kafka configuration properties:

- The consumer group id in case our application consumes events (e.g. kafka.account-updated-consumer.consumer.group-id=account-updated-event).

### How to override default kafka configuration properties

To define custom configuration properties per producer/consumer service, there exist two basic classes: NetzKafkaProducerProperties and NetzKafkaConsumerProperties , that should be extended by concrete classes.

NetzKafkaConsumerProperties:

    consumer // By setting any of the consumer's properties we can override the default consumer settings. 
    retryInterval // the time delay between retry attempts when a consumer fails to process a message
    retryMaxAttempts // the maximum number of retries for the listener
    dlqProducer // specifies the properties for the Dead Letter Queue (DLQ) Producer. If not set, the default spring kafka producer properties will be applied

### Consumer properties that are enforced

    - ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG = ErrorHandlingDeserializer.class
    - ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS = JsonDeserializer.class.getName()
    - ConsumerConfig.ISOLATION_LEVEL_CONFIG = IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT)
    - missingTopicsFatal = true
    - All exceptions are considered non-retryable by default except KafkaRetryableException
    - Dead Letter Queue is created for all topics

## Topic creation

Automatic topic creation is disabled (see [docker-compose file](https://git.trasys.gr/bitbucket/projects/UKNETZ/repos/uk-netz-env-development/browse/docker-compose.yml) of uk-netz-development repo).

To create a topic named 'foo-topic', the following command should be executed from the uk-netz-development repo:

    docker-compose exec kafka kafka-topics.sh --create --topic foo-topic --partitions 1 --replication-factor 1 --if-not-exists --bootstrap-server localhost:9092

For each topic created, its corresponding DLQ topic should be created as well. The DLQ topic name should be the name of the topic with the suffix .DLT. For example to create the DLQ topic for the 'foo-topic', the following command should be executed:

    docker-compose exec kafka kafka-topics.sh --create --topic foo-topic.DLT --partitions 1 --replication-factor 1 --if-not-exists --bootstrap-server localhost:9092