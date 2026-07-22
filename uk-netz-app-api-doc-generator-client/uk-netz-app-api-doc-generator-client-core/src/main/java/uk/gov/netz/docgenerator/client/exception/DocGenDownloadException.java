package uk.gov.netz.docgenerator.client.exception;

public class DocGenDownloadException extends DocGenClientException {

    public DocGenDownloadException(String message) {
        super(message);
    }

    public DocGenDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
