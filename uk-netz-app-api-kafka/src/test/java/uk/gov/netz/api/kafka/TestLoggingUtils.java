package uk.gov.netz.api.kafka;


import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.MementoMessage;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.message.ObjectMessage;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestLoggingUtils {

    public static List<LogEntry> getLoggingEntries(ListAppender listAppender) {
        return listAppender.getEvents().stream()
                .map(event -> {
                    if (event.getMessage() instanceof MementoMessage mementoMessage) {
                        List<Object> parameters = Arrays.asList(mementoMessage.getParameters());
                        return parameters.stream()
                                .filter(parameter -> parameter instanceof KafkaLoggingEntry)
                                .map(parameter -> LogEntry.builder().logLevel(event.getLevel()).logEntry((KafkaLoggingEntry)parameter).build())
                                .collect(Collectors.toList());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Data
    @Builder
    public static class LogEntry {
        private Level logLevel;
        private KafkaLoggingEntry logEntry;
    }
}
