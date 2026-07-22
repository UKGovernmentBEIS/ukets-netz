package uk.gov.netz.docgenerator.client.status;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.model.JobState;
import uk.gov.netz.docgenerator.client.model.JobStatus;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.OutputObject;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.storage.StatusMarkerObject;

@RequiredArgsConstructor
public class DefaultJobStatusResolver {

    private final DocumentStorageClient documentStorageClient;

    public JobStatus getStatus(@NonNull String jobId) {
        List<OutputObject> outputObjects = documentStorageClient.listOutputObjects(jobId);
        List<StatusMarkerObject> statusMarkers = documentStorageClient.listStatusMarkers(jobId);
        Map<StatusMarker, StatusMarkerObject> markerMap = markersByType(statusMarkers);
        Instant submittedAt = submittedAt(markerMap);

        if (containsObject(outputObjects, DocumentObjectKeys.outputPdf(jobId))) {
            return new JobStatus(jobId, JobState.COMPLETE, DocumentObjectKeys.outputPdf(jobId), null, submittedAt);
        }

        if (containsObject(outputObjects, DocumentObjectKeys.errorJson(jobId))) {
            return new JobStatus(jobId, JobState.FAILED, null, errorDetail(jobId), submittedAt);
        }

        if (markerMap.containsKey(StatusMarker.PROCESSING)) {
            return new JobStatus(jobId, JobState.PROCESSING, null, null, submittedAt);
        }

        if (markerMap.containsKey(StatusMarker.SUBMITTED)) {
            return new JobStatus(jobId, JobState.QUEUED, null, null, submittedAt);
        }

        if (markerMap.containsKey(StatusMarker.SUBMISSION_FAILED)) {
            return new JobStatus(jobId, JobState.SUBMISSION_FAILED, null, errorDetail(jobId), submittedAt);
        }

        if (markerMap.containsKey(StatusMarker.UPLOADED)) {
            return new JobStatus(jobId, JobState.PENDING, null, null, submittedAt);
        }

        return new JobStatus(jobId, JobState.NOT_FOUND, null, null, submittedAt);
    }

    private Map<StatusMarker, StatusMarkerObject> markersByType(List<StatusMarkerObject> statusMarkers) {
        Map<StatusMarker, StatusMarkerObject> markerMap = new EnumMap<>(StatusMarker.class);
        for (StatusMarkerObject statusMarker : statusMarkers) {
            markerMap.merge(statusMarker.getMarker(), statusMarker, this::latestMarker);
        }
        return markerMap;
    }

    private StatusMarkerObject latestMarker(StatusMarkerObject current, StatusMarkerObject candidate) {
        if (candidate.getLastModified() == null) {
            return current;
        }
        if (current.getLastModified() == null) {
            return candidate;
        }
        return candidate.getLastModified().isAfter(current.getLastModified()) ? candidate : current;
    }

    private Instant submittedAt(Map<StatusMarker, StatusMarkerObject> markerMap) {
        StatusMarkerObject submittedMarker = markerMap.get(StatusMarker.SUBMITTED);
        return submittedMarker == null ? null : submittedMarker.getLastModified();
    }

    private boolean containsObject(List<OutputObject> outputObjects, String objectKey) {
        return outputObjects.stream().anyMatch(outputObject -> objectKey.equals(outputObject.getObjectKey()));
    }

    private ErrorDetail errorDetail(String jobId) {
        return documentStorageClient.readErrorDetail(jobId).orElse(null);
    }
}
