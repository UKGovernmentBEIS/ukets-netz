package uk.gov.netz.docgenerator.client.exception;

public class DocGenClientException extends RuntimeException {

    public DocGenClientException(String message) {
        super(message);
    }

    public DocGenClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
