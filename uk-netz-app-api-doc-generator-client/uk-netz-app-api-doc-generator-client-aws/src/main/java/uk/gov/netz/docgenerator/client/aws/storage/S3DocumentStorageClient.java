package uk.gov.netz.docgenerator.client.aws.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.docgenerator.client.exception.DocGenClientException;
import uk.gov.netz.docgenerator.client.exception.DocGenDownloadException;
import uk.gov.netz.docgenerator.client.exception.DocGenUploadException;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.OutputObject;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.storage.StatusMarkerObject;

@RequiredArgsConstructor
public class S3DocumentStorageClient implements DocumentStorageClient {

    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String MARKER_CONTENT_TYPE = "application/octet-stream";
    private final S3Operations s3Operations;
    private final String bucket;

    @Override
    public void uploadDocx(String jobId, @NonNull byte[] docxBytes) {
        upload(DocumentObjectKeys.inputDocx(jobId), docxBytes, DOCX_CONTENT_TYPE);
    }

    @Override
    public void writeStatusMarker(String jobId, StatusMarker marker) {
        upload(DocumentObjectKeys.statusMarker(jobId, marker), new byte[0], MARKER_CONTENT_TYPE);
    }

    @Override
    public List<StatusMarkerObject> listStatusMarkers(String jobId) {
        String prefix = DocumentObjectKeys.statusPrefix(jobId);
        try {
            return s3Operations.listObjects(bucket, prefix).stream()
                .map(resource -> toStatusMarkerObject(jobId, resource))
                .flatMap(Optional::stream)
                .toList();
        } catch (RuntimeException ex) {
            throw new DocGenClientException("Failed to list document generation status markers for job " + jobId, ex);
        }
    }

    @Override
    public List<OutputObject> listOutputObjects(String jobId) {
        String prefix = DocumentObjectKeys.outputPrefix(jobId);
        try {
            return s3Operations.listObjects(bucket, prefix).stream()
                .map(resource -> new OutputObject(resource.getFilename(), lastModified(resource)))
                .toList();
        } catch (RuntimeException ex) {
            throw new DocGenClientException("Failed to list document generation output objects for job " + jobId, ex);
        }
    }

    @Override
    public boolean pdfExists(String jobId) {
        return objectExists(DocumentObjectKeys.outputPdf(jobId));
    }

    @Override
    public boolean errorJsonExists(String jobId) {
        return objectExists(DocumentObjectKeys.errorJson(jobId));
    }

    @Override
    public byte[] downloadPdf(String jobId) {
        String key = DocumentObjectKeys.outputPdf(jobId);
        try (var inputStream = s3Operations.download(bucket, key).getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException | RuntimeException ex) {
            throw new DocGenDownloadException("Failed to download document generation PDF for job " + jobId, ex);
        }
    }

    @Override
    public Optional<ErrorDetail> readErrorDetail(String jobId) {
        String key = DocumentObjectKeys.errorJson(jobId);
        if (!objectExists(key)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(s3Operations.read(bucket, key, ErrorDetail.class));
        } catch (RuntimeException ex) {
            throw new DocGenDownloadException("Failed to read document generation error detail for job " + jobId, ex);
        }
    }

    @Override
    public Optional<URI> createDownloadUrl(String jobId, @NonNull Duration expiry) {
        String key = DocumentObjectKeys.outputPdf(jobId);
        try {
            return Optional.of(URI.create(s3Operations.createSignedGetURL(bucket, key, expiry).toString()));
        } catch (RuntimeException ex) {
            throw new DocGenDownloadException("Failed to create document generation PDF download URL for job " + jobId, ex);
        }
    }

    private void upload(String key, byte[] bytes, String contentType) {
        try {
            ObjectMetadata metadata = ObjectMetadata.builder()
                .contentLength((long) bytes.length)
                .contentType(contentType)
                .build();
            s3Operations.upload(bucket, key, new ByteArrayInputStream(bytes), metadata);
        } catch (RuntimeException ex) {
            throw new DocGenUploadException("Failed to upload document generation object " + key, ex);
        }
    }

    private boolean objectExists(String key) {
        try {
            return s3Operations.objectExists(bucket, key);
        } catch (RuntimeException ex) {
            throw new DocGenClientException("Failed to check document generation object " + key, ex);
        }
    }

    private Optional<StatusMarkerObject> toStatusMarkerObject(String jobId, S3Resource resource) {
        String objectKey = resource.getFilename();
        return DocumentObjectKeys.parseStatusMarker(jobId, objectKey)
            .map(marker -> new StatusMarkerObject(marker, objectKey, lastModified(resource)));
    }

    private Instant lastModified(S3Resource resource) {
        try {
            return Instant.ofEpochMilli(resource.lastModified());
        } catch (IOException ex) {
            throw new DocGenClientException("Failed to read document generation marker metadata for " + resource.getFilename(), ex);
        }
    }
}
