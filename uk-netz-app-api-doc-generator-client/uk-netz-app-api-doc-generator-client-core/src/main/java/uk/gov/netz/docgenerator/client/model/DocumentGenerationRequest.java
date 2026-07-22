package uk.gov.netz.docgenerator.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used by {@code DocumentGeneratorClient} to submit a DOCX document for PDF generation.
 *
 * <p>The DOCX bytes are the only required business value and are validated when a request is submitted. Priority defaults to
 * {@link DocumentGenerationPriority#HIGH}; callers can choose {@link DocumentGenerationPriority#LOW} for work that should use the low-priority
 * queue message group. Optional metadata is sent to the worker as business context and should be copied into async result events by the document
 * generator worker. The {@code normalize} flag asks the worker to normalize DOCX content before conversion.</p>
 *
 * <p>Instances are mutable for simple Spring and Lombok usage. Callers should treat a request as owned by one submission and avoid mutating it
 * after passing it to the client.</p>
 */
@Data
@NoArgsConstructor
public class DocumentGenerationRequest {

    /**
     * DOCX file contents to upload for conversion.
     *
     * <p>This value must be non-null when the request is submitted. Empty byte arrays are not rejected by the client, but the worker may fail them
     * if they do not contain a valid DOCX document.</p>
     */
    private byte[] docxBytes;

    /**
     * Submission priority used to choose the queue message group.
     *
     * <p>{@code null} is treated as {@link DocumentGenerationPriority#HIGH} by the builder and by submission validation.</p>
     */
    private DocumentGenerationPriority priority = DocumentGenerationPriority.HIGH;

    /**
     * Optional business metadata sent with the queue message.
     *
     * <p>Metadata values are not interpreted by this client. They are intended for consuming backends to correlate async result events with their
     * own domain records.</p>
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * Whether the worker should normalize DOCX content before conversion.
     *
     * <p>The flag defaults to {@code false}. When false, the queue message omits the field and the worker performs its default conversion path.</p>
     */
    private boolean normalize;

    /**
     * Creates a high-priority request for the supplied DOCX bytes.
     *
     * @param docxBytes DOCX file contents to convert; must be non-null when submitted
     */
    public DocumentGenerationRequest(byte[] docxBytes) {
        this.docxBytes = docxBytes;
    }

    /**
     * Creates a request for the supplied DOCX bytes and priority.
     *
     * <p>Passing {@code null} for {@code priority} is accepted. Submission validation treats it as
     * {@link DocumentGenerationPriority#HIGH}.</p>
     *
     * @param docxBytes DOCX file contents to convert; must be non-null when submitted
     * @param priority requested submission priority, or {@code null} to use high priority
     */
    public DocumentGenerationRequest(byte[] docxBytes, DocumentGenerationPriority priority) {
        this(docxBytes);
        this.priority = priority;
    }

    /**
     * Creates a request with all supported submission options.
     *
     * <p>This constructor is also used by the Lombok-generated builder. Passing {@code null} metadata stores an empty map. Passing a metadata map
     * copies its current entries into a new {@link LinkedHashMap}, so later mutations to the caller's map do not affect the request.</p>
     *
     * @param docxBytes DOCX file contents to convert; must be non-null when submitted
     * @param priority requested submission priority, or {@code null} to use high priority
     * @param metadata optional business metadata to send with the job
     * @param normalize whether to ask the worker to normalize DOCX content before conversion
     */
    @Builder
    public DocumentGenerationRequest(
        byte[] docxBytes,
        DocumentGenerationPriority priority,
        Map<String, String> metadata,
        boolean normalize
    ) {
        this.docxBytes = docxBytes;
        this.priority = priority == null ? DocumentGenerationPriority.HIGH : priority;
        setMetadata(metadata);
        this.normalize = normalize;
    }

    /**
     * Replaces optional business metadata for the request.
     *
     * <p>The supplied map is defensively copied into insertion order. Passing {@code null} clears the metadata to an empty map. Metadata is
     * serialized on the queue message and should be copied by the worker into async result events.</p>
     *
     * @param metadata optional metadata values keyed by backend-defined names, or {@code null} for no metadata
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }
}
