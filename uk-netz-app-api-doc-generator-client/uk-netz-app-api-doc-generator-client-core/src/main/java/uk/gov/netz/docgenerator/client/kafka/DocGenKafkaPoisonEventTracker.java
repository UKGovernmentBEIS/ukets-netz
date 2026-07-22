package uk.gov.netz.docgenerator.client.kafka;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;

public class DocGenKafkaPoisonEventTracker {

    private final ConcurrentMap<PoisonEventKey, AtomicInteger> failures = new ConcurrentHashMap<>();

    public int recordFailure(ConsumerRecord<String, ConversionEvent> record) {
        return failures.computeIfAbsent(PoisonEventKey.from(record), key -> new AtomicInteger()).incrementAndGet();
    }

    public void clear(ConsumerRecord<String, ConversionEvent> record) {
        failures.remove(PoisonEventKey.from(record));
    }

    private record PoisonEventKey(String topic, int partition, long offset, String jobId) {

        private static PoisonEventKey from(@NonNull ConsumerRecord<String, ConversionEvent> record) {
            ConversionEvent event = record.value();
            String jobId = event == null ? record.key() : event.getJobId();
            return new PoisonEventKey(record.topic(), record.partition(), record.offset(), jobId);
        }
    }
}
