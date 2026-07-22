package uk.gov.netz.docgenerator.client;

import java.net.URI;
import java.time.Duration;

import uk.gov.netz.docgenerator.client.async.DefaultAsyncJobManager;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.download.DefaultDocumentDownloadClient;
import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.ConversionResult;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.JobStatus;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.status.DefaultJobStatusResolver;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.sync.DefaultSyncJobManager;

/**
 * Main facade used by consuming backend applications to submit DOCX documents to the document generator service and retrieve generated PDF
 * results.
 *
 * <p>The client is provider-neutral. Concrete storage and queue access is supplied through the configured {@link DocumentStorageClient} and
 * {@link JobQueuePublisher} implementations, normally contributed by a provider adapter such as the AWS module. Instances are thread-safe when
 * the configured storage and queue clients are thread-safe.</p>
 */
public class DocumentGeneratorClient {

    private final DefaultSyncJobManager syncJobManager;
    private final DefaultAsyncJobManager asyncJobManager;
    private final DefaultJobStatusResolver jobStatusResolver;
    private final DefaultDocumentDownloadClient documentDownloadClient;

    /**
     * Creates a client facade backed by the supplied storage and queue adapters.
     *
     * @param documentStorageClient storage adapter used to upload input DOCX files, inspect job status markers, and read generated outputs
     * @param jobQueuePublisher queue adapter used to publish synchronous and asynchronous conversion jobs
     * @param properties client configuration, including queue destinations, timeouts, message group ID, and download URL expiry
     * @throws NullPointerException if any required dependency is {@code null}
     * @throws IllegalArgumentException if required submission configuration such as queue destinations is blank
     */
    public DocumentGeneratorClient(
        DocumentStorageClient documentStorageClient,
        JobQueuePublisher jobQueuePublisher,
        DocGenClientProperties properties
    ) {
        this(
            new DefaultSyncJobManager(documentStorageClient, jobQueuePublisher, properties),
            new DefaultAsyncJobManager(documentStorageClient, jobQueuePublisher, properties),
            new DefaultJobStatusResolver(documentStorageClient),
            new DefaultDocumentDownloadClient(documentStorageClient, properties)
        );
    }

    DocumentGeneratorClient(
        DefaultSyncJobManager syncJobManager,
        DefaultAsyncJobManager asyncJobManager,
        DefaultJobStatusResolver jobStatusResolver,
        DefaultDocumentDownloadClient documentDownloadClient
    ) {
        this.syncJobManager = syncJobManager;
        this.asyncJobManager = asyncJobManager;
        this.jobStatusResolver = jobStatusResolver;
        this.documentDownloadClient = documentDownloadClient;
    }

    /**
     * Submits a DOCX document for synchronous conversion and waits until a terminal result is observed or the configured sync timeout expires.
     *
     * <p>The client uploads the request DOCX bytes, writes status markers, publishes a queue message, and polls storage for either
     * {@code output/{jobId}.pdf} or {@code output/{jobId}.error.json}. The returned value is one of:</p>
     *
     * <ul>
     *   <li>{@link ConversionResult.Success} when the generated PDF exists.</li>
     *   <li>{@link ConversionResult.Failed} when the worker wrote an error object.</li>
     *   <li>{@link ConversionResult.Timeout} when no terminal output object appears before the configured timeout.</li>
     * </ul>
     *
     * @param request document generation request containing the DOCX bytes and optional priority, metadata, and normalization flag
     * @return the synchronous conversion result
     * @throws IllegalArgumentException if {@code request} or its DOCX bytes are {@code null}
     * @throws uk.gov.netz.docgenerator.client.exception.DocGenSubmitException if the waiting thread is interrupted
     * @throws RuntimeException if upload, queue publication, status marker writes, or result inspection fail
     */
    public ConversionResult submit(DocumentGenerationRequest request) {
        return syncJobManager.submit(request);
    }

    /**
     * Submits a DOCX document for asynchronous conversion and returns immediately with the generated job ID.
     *
     * <p>The client uploads the DOCX, writes status markers, and publishes the async queue message before returning. The returned receipt is not a
     * future and does not represent completion of the conversion. Consuming applications should persist the returned job ID and later use
     * {@link #getStatus(String)}, Kafka result events, or download helpers once the job has completed.</p>
     *
     * @param request document generation request containing the DOCX bytes and optional priority, metadata, and normalization flag
     * @return receipt containing the generated job ID
     * @throws IllegalArgumentException if {@code request} or its DOCX bytes are {@code null}
     * @throws RuntimeException if upload, queue publication, or status marker writes fail
     */
    public AsyncJobReceipt submitAsync(DocumentGenerationRequest request) {
        return asyncJobManager.submit(request);
    }

    /**
     * Resolves the current status of a document generation job from storage markers and output objects.
     *
     * <p>Status resolution is read-only and does not contact the queue or worker. A missing job ID resolves to a {@link JobStatus} with
     * {@code JobState.NOT_FOUND} when no known markers or output objects exist for the job.</p>
     *
     * @param jobId generated document generation job ID
     * @return current job status derived from storage state
     * @throws NullPointerException if {@code jobId} is {@code null}
     * @throws RuntimeException if storage inspection fails
     */
    public JobStatus getStatus(String jobId) {
        return jobStatusResolver.getStatus(jobId);
    }

    /**
     * Downloads the generated PDF bytes for a completed document generation job.
     *
     * <p>The job must already have completed successfully. Use {@link #getStatus(String)} or a {@link ConversionResult.Success} returned by
     * {@link #submit(DocumentGenerationRequest)} to determine when the PDF is available.</p>
     *
     * @param jobId generated document generation job ID
     * @return generated PDF bytes
     * @throws NullPointerException if {@code jobId} is {@code null}
     * @throws RuntimeException if the PDF does not exist or cannot be downloaded by the configured storage adapter
     */
    public byte[] downloadBytes(String jobId) {
        return documentDownloadClient.downloadBytes(jobId);
    }

    /**
     * Creates a time-limited download URL for the generated PDF when supported by the configured storage adapter.
     *
     * <p>Passing {@code null} for {@code expiry} uses the configured {@code docgen.client.download-url-expiry} value. Not every storage adapter can
     * create signed URLs; unsupported adapters throw a
     * {@link uk.gov.netz.docgenerator.client.exception.DocGenDownloadException}.</p>
     *
     * @param jobId generated document generation job ID
     * @param expiry requested URL lifetime, or {@code null} to use the configured default
     * @return signed URI for downloading the generated PDF
     * @throws NullPointerException if {@code jobId} is {@code null}
     * @throws uk.gov.netz.docgenerator.client.exception.DocGenDownloadException if signed URLs are not supported by the configured storage adapter
     * @throws RuntimeException if signed URL creation fails
     */
    public URI createDownloadUrl(String jobId, Duration expiry) {
        return documentDownloadClient.createDownloadUrl(jobId, expiry);
    }
}
