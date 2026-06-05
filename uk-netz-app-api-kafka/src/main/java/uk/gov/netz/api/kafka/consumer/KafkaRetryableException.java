package uk.gov.netz.api.kafka.consumer;

import java.io.Serial;

public class KafkaRetryableException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public KafkaRetryableException(String message) {
		super(message);
	}

	public KafkaRetryableException(Throwable cause) {
		super(cause);
	}
	
	public KafkaRetryableException(String message, Throwable cause) {
		super(message, cause);
	}

}
