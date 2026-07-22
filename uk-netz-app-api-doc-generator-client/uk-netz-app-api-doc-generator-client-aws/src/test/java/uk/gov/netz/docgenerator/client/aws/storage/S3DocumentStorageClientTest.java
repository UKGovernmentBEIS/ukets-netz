package uk.gov.netz.docgenerator.client.aws.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;

class S3DocumentStorageClientTest {

    @Test
    void uploadDocxStoresExpectedKeyBytesAndMetadata() throws Exception {
        S3Operations s3Operations = mock(S3Operations.class);
        S3DocumentStorageClient storageClient = new S3DocumentStorageClient(s3Operations, "docgen-bucket");
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

        storageClient.uploadDocx("job-1", new byte[] {1, 2, 3});

        verify(s3Operations).upload(
            eq("docgen-bucket"),
            eq(DocumentObjectKeys.inputDocx("job-1")),
            inputStreamCaptor.capture(),
            metadataCaptor.capture()
        );
        assertThat(inputStreamCaptor.getValue().readAllBytes()).containsExactly(1, 2, 3);
        assertThat(metadataCaptor.getValue().getContentLength()).isEqualTo(3L);
        assertThat(metadataCaptor.getValue().getContentType())
            .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void writeStatusMarkerStoresZeroByteMarker() throws Exception {
        S3Operations s3Operations = mock(S3Operations.class);
        S3DocumentStorageClient storageClient = new S3DocumentStorageClient(s3Operations, "docgen-bucket");
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMITTED);

        verify(s3Operations).upload(
            eq("docgen-bucket"),
            eq(DocumentObjectKeys.statusMarker("job-1", StatusMarker.SUBMITTED)),
            inputStreamCaptor.capture(),
            metadataCaptor.capture()
        );
        assertThat(inputStreamCaptor.getValue().readAllBytes()).isEmpty();
        assertThat(metadataCaptor.getValue().getContentLength()).isZero();
        assertThat(metadataCaptor.getValue().getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void listsStatusMarkersAndIgnoresUnknownObjects() throws Exception {
        S3Operations s3Operations = mock(S3Operations.class);
        S3Resource uploaded = resource(DocumentObjectKeys.statusMarker("job-1", StatusMarker.UPLOADED), 1_000L);
        S3Resource nested = resource("status/job-1/nested/object", 2_000L);
        when(s3Operations.listObjects("docgen-bucket", DocumentObjectKeys.statusPrefix("job-1")))
            .thenReturn(List.of(uploaded, nested));

        assertThat(new S3DocumentStorageClient(s3Operations, "docgen-bucket").listStatusMarkers("job-1"))
            .singleElement()
            .satisfies(marker -> {
                assertThat(marker.getMarker()).isEqualTo(StatusMarker.UPLOADED);
                assertThat(marker.getObjectKey()).isEqualTo(DocumentObjectKeys.statusMarker("job-1", StatusMarker.UPLOADED));
                assertThat(marker.getLastModified()).isEqualTo(Instant.ofEpochMilli(1_000L));
            });
    }

    @Test
    void downloadsPdfReadsErrorDetailAndCreatesSignedUrl() throws Exception {
        S3Operations s3Operations = mock(S3Operations.class);
        S3Resource pdfResource = mock(S3Resource.class);
        ErrorDetail errorDetail = ErrorDetail.builder().jobId("job-1").errorReason("failed").build();
        when(s3Operations.download("docgen-bucket", DocumentObjectKeys.outputPdf("job-1"))).thenReturn(pdfResource);
        when(pdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2}));
        when(s3Operations.objectExists("docgen-bucket", DocumentObjectKeys.errorJson("job-1"))).thenReturn(true);
        when(s3Operations.read("docgen-bucket", DocumentObjectKeys.errorJson("job-1"), ErrorDetail.class)).thenReturn(errorDetail);
        when(s3Operations.createSignedGetURL("docgen-bucket", DocumentObjectKeys.outputPdf("job-1"), Duration.ofMinutes(5)))
            .thenReturn(new URL("https://example.test/job-1.pdf"));

        S3DocumentStorageClient storageClient = new S3DocumentStorageClient(s3Operations, "docgen-bucket");

        assertThat(storageClient.downloadPdf("job-1")).containsExactly(1, 2);
        assertThat(storageClient.readErrorDetail("job-1")).contains(errorDetail);
        assertThat(storageClient.createDownloadUrl("job-1", Duration.ofMinutes(5)))
            .contains(URI.create("https://example.test/job-1.pdf"));
    }

    private static S3Resource resource(String filename, long lastModified) throws Exception {
        S3Resource resource = mock(S3Resource.class);
        when(resource.getFilename()).thenReturn(filename);
        when(resource.lastModified()).thenReturn(lastModified);
        return resource;
    }
}
