package uk.gov.netz.docgenerator.client.exception;

public class DocGenUploadException extends DocGenClientException {

    public DocGenUploadException(String message) {
        super(message);
    }

    public DocGenUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
