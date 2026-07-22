package uk.gov.netz.docgenerator.client.exception;

public class DocGenSubmitException extends DocGenClientException {

    public DocGenSubmitException(String message) {
        super(message);
    }

    public DocGenSubmitException(String message, Throwable cause) {
        super(message, cause);
    }
}
