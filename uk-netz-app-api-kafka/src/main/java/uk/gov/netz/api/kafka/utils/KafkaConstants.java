package uk.gov.netz.api.kafka.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstants {

	public final String CORRELATION_ID_HEADER = "Correlation-Id";
	public final String CORRELATION_PARENT_ID_HEADER = "Correlation-Parent-Id";
	public final String PRODUCER_CLIENT_ID_HEADER = "Producer-Client-Id";

	public final String PRODUCER_MAX_AGE_SECONDS = "max-age-seconds";
	public final Long DEFAULT_PRODUCER_MAX_AGE_SECONDS = (long) (24 * 60 * 60); // 1 day

}
