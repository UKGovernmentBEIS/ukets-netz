package uk.gov.netz.docgenerator.client.support;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import uk.gov.netz.docgenerator.client.exception.DocGenDownloadException;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.OutputObject;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.storage.StatusMarkerObject;

public class InMemoryDocumentStorageClient implements DocumentStorageClient {

    private final Map<String, byte[]> docxObjects = new HashMap<>();
    private final Map<String, byte[]> pdfObjects = new HashMap<>();
    private final Map<String, ErrorDetail> errorObjects = new HashMap<>();
    private final Map<String, List<StatusMarkerObject>> statusMarkers = new HashMap<>();
    private final List<String> operations = new ArrayList<>();
    private final EnumSet<StatusMarker> failingMarkers = EnumSet.noneOf(StatusMarker.class);
    private Instant nextLastModified = Instant.parse("2026-05-12T00:00:00Z");
    private URI downloadUrl;

    @Override
    public void uploadDocx(String jobId, byte[] docxBytes) {
        operations.add("uploadDocx:" + jobId);
        docxObjects.put(jobId, Arrays.copyOf(docxBytes, docxBytes.length));
    }

    @Override
    public void writeStatusMarker(String jobId, StatusMarker marker) {
        operations.add("writeStatusMarker:" + jobId + ":" + marker);
        if (failingMarkers.contains(marker)) {
            throw new IllegalStateException("marker failed: " + marker);
        }
        statusMarkers.computeIfAbsent(jobId, ignored -> new ArrayList<>())
            .add(new StatusMarkerObject(marker, DocumentObjectKeys.statusMarker(jobId, marker), nextInstant()));
    }

    @Override
    public List<StatusMarkerObject> listStatusMarkers(String jobId) {
        operations.add("listStatusMarkers:" + jobId);
        return List.copyOf(statusMarkers.getOrDefault(jobId, List.of()));
    }

    @Override
    public List<OutputObject> listOutputObjects(String jobId) {
        operations.add("listOutputObjects:" + jobId);
        List<OutputObject> objects = new ArrayList<>();
        if (pdfObjects.containsKey(jobId)) {
            objects.add(new OutputObject(DocumentObjectKeys.outputPdf(jobId), nextInstant()));
        }
        if (errorObjects.containsKey(jobId)) {
            objects.add(new OutputObject(DocumentObjectKeys.errorJson(jobId), nextInstant()));
        }
        return objects;
    }

    @Override
    public boolean pdfExists(String jobId) {
        operations.add("pdfExists:" + jobId);
        return pdfObjects.containsKey(jobId);
    }

    @Override
    public boolean errorJsonExists(String jobId) {
        operations.add("errorJsonExists:" + jobId);
        return errorObjects.containsKey(jobId);
    }

    @Override
    public byte[] downloadPdf(String jobId) {
        operations.add("downloadPdf:" + jobId);
        byte[] pdfBytes = pdfObjects.get(jobId);
        if (pdfBytes == null) {
            throw new DocGenDownloadException("Missing PDF for job " + jobId);
        }
        return Arrays.copyOf(pdfBytes, pdfBytes.length);
    }

    @Override
    public Optional<ErrorDetail> readErrorDetail(String jobId) {
        operations.add("readErrorDetail:" + jobId);
        return Optional.ofNullable(errorObjects.get(jobId));
    }

    @Override
    public Optional<URI> createDownloadUrl(String jobId, Duration expiry) {
        operations.add("createDownloadUrl:" + jobId + ":" + expiry);
        return Optional.ofNullable(downloadUrl);
    }

    public List<String> operations() {
        return List.copyOf(operations);
    }

    public byte[] docx(String jobId) {
        return docxObjects.get(jobId);
    }

    public void putPdf(String jobId, byte[] pdfBytes) {
        pdfObjects.put(jobId, Arrays.copyOf(pdfBytes, pdfBytes.length));
    }

    public void putError(String jobId, ErrorDetail errorDetail) {
        errorObjects.put(jobId, errorDetail);
    }

    public void failMarker(StatusMarker marker) {
        failingMarkers.add(marker);
    }

    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    private Instant nextInstant() {
        Instant current = nextLastModified;
        nextLastModified = nextLastModified.plusSeconds(1);
        return current;
    }
}
