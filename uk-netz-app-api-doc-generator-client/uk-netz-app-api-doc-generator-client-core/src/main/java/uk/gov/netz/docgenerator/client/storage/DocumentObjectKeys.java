package uk.gov.netz.docgenerator.client.storage;

import java.util.Objects;
import java.util.Optional;

import lombok.NonNull;

public final class DocumentObjectKeys {

    private static final String INPUT_PREFIX = "input/";
    private static final String STATUS_PREFIX = "status/";
    private static final String OUTPUT_PREFIX = "output/";

    private DocumentObjectKeys() {
    }

    public static String inputDocx(String jobId) {
        return INPUT_PREFIX + requiredJobId(jobId) + ".docx";
    }

    public static String statusPrefix(String jobId) {
        return STATUS_PREFIX + requiredJobId(jobId) + "/";
    }

    public static String statusMarker(String jobId, @NonNull StatusMarker marker) {
        return statusPrefix(jobId) + marker.objectName();
    }

    public static String outputPrefix(String jobId) {
        return OUTPUT_PREFIX + requiredJobId(jobId);
    }

    public static String outputPdf(String jobId) {
        return outputPrefix(jobId) + ".pdf";
    }

    public static String errorJson(String jobId) {
        return outputPrefix(jobId) + ".error.json";
    }

    public static Optional<StatusMarker> parseStatusMarker(String jobId, String objectKey) {
        String prefix = statusPrefix(jobId);
        if (objectKey == null || !objectKey.startsWith(prefix)) {
            return Optional.empty();
        }

        String markerName = objectKey.substring(prefix.length());
        if (markerName.isBlank() || markerName.contains("/")) {
            return Optional.empty();
        }
        return StatusMarker.fromObjectName(markerName);
    }

    private static String requiredJobId(String jobId) {
        Objects.requireNonNull(jobId, "jobId");
        if (jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be blank");
        }
        return jobId;
    }
}
