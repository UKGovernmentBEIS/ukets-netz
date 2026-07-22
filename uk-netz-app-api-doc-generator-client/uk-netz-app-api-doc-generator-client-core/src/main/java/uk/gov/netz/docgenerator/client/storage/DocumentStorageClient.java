package uk.gov.netz.docgenerator.client.storage;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import uk.gov.netz.docgenerator.client.model.ErrorDetail;

public interface DocumentStorageClient {

    void uploadDocx(String jobId, byte[] docxBytes);

    void writeStatusMarker(String jobId, StatusMarker marker);

    List<StatusMarkerObject> listStatusMarkers(String jobId);

    List<OutputObject> listOutputObjects(String jobId);

    boolean pdfExists(String jobId);

    boolean errorJsonExists(String jobId);

    byte[] downloadPdf(String jobId);

    Optional<ErrorDetail> readErrorDetail(String jobId);

    Optional<URI> createDownloadUrl(String jobId, Duration expiry);
}
