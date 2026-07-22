package uk.gov.netz.docgenerator.client.kafka;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import uk.gov.netz.docgenerator.client.ConversionResultHandler;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;

@Log4j2
public class DocGenKafkaResultConsumer {

    private final List<ConversionResultHandler> handlers;
    private final DocGenKafkaPoisonEventTracker poisonEventTracker;
    private final int maxHandlerRetries;

    public DocGenKafkaResultConsumer(
        @NonNull List<ConversionResultHandler> handlers,
        @NonNull DocGenKafkaPoisonEventTracker poisonEventTracker,
        @NonNull DocGenClientProperties properties
    ) {
        this.handlers = new ArrayList<>(handlers);
        AnnotationAwareOrderComparator.sort(this.handlers);
        this.poisonEventTracker = poisonEventTracker;
        this.maxHandlerRetries = properties.getMaxHandlerRetries();
    }

    @KafkaListener(
        id = "docGenKafkaResultConsumer",
        topics = "${kafka.docgen-consumer.topic:doc.converted}",
        groupId = "${kafka.docgen-consumer.group:app-api-doc-result-consumer}",
        containerFactory = "docGenKafkaListenerContainerFactory"
    )
    public void onConversionEvent(
        @NonNull ConsumerRecord<String, ConversionEvent> record,
        @NonNull Acknowledgment acknowledgment
    ) {
        ConversionEvent event = record.value();
        if (event == null) {
            log.debug(
                "Skipping null document generation conversion event for topic {}, partition {}, offset {}",
                record.topic(),
                record.partition(),
                record.offset()
            );
            poisonEventTracker.clear(record);
            acknowledgment.acknowledge();
            return;
        }
        String jobId = event.getJobId();
        if (handlers.isEmpty()) {
            log.debug("No document generation conversion result handlers registered for jobId {}", jobId);
            poisonEventTracker.clear(record);
            acknowledgment.acknowledge();
            return;
        }

        if (invokeHandlers(event)) {
            poisonEventTracker.clear(record);
            acknowledgment.acknowledge();
            return;
        }

        int failureCount = poisonEventTracker.recordFailure(record);
        if (failureCount >= maxHandlerRetries) {
            log.error(
                "Skipping poison document generation conversion event after {} handler failures for topic {}, partition {}, offset {}, jobId {}",
                failureCount,
                record.topic(),
                record.partition(),
                record.offset(),
                jobId
            );
            poisonEventTracker.clear(record);
            acknowledgment.acknowledge();
            return;
        }

        throw new DocGenKafkaHandlerException("Document generation conversion result handlers failed for jobId " + jobId);
    }

    private boolean invokeHandlers(ConversionEvent event) {
        boolean allHandlersSucceeded = true;
        for (ConversionResultHandler handler : handlers) {
            try {
                handler.handle(event);
            } catch (RuntimeException ex) {
                allHandlersSucceeded = false;
                log.warn(
                    "Document generation conversion result handler failed for jobId {} and handler {}",
                    event.getJobId(),
                    handler.getClass().getName(),
                    ex
                );
            }
        }
        return allHandlersSucceeded;
    }
}
