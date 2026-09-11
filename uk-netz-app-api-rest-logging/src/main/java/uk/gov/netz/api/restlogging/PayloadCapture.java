package uk.gov.netz.api.restlogging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

final class PayloadCapture {

    private final int limitBytes;

    private long expectedSize;
    private CaptureBuffer buffer;
    private long sizeBytes;
    private boolean truncated;
    private boolean complete;
    private String skipReason;

    PayloadCapture(int limitBytes, long expectedSize) {
        this.limitBytes = limitBytes;
        reset(expectedSize);
    }

    void reset(long expectedSize) {
        this.expectedSize = expectedSize;
        buffer = null;
        sizeBytes = 0;
        truncated = false;
        complete = false;
        skipReason = null;
        if (expectedSize > limitBytes) {
            truncated = true;
        } else if (limitBytes > 0) {
            int initialSize = expectedSize > 0
                    ? (int) Math.min(expectedSize, 8192)
                    : Math.min(1024, limitBytes);
            buffer = new CaptureBuffer(initialSize);
        }
    }

    void accept(int value) {
        sizeBytes++;
        if (skipReason != null || truncated) {
            return;
        }
        if (sizeBytes > limitBytes) {
            markTruncated();
            return;
        }
        buffer.write(value);
    }

    void accept(byte[] content, int offset, int length) {
        if (length == 0) {
            return;
        }
        sizeBytes += length;
        if (skipReason != null || truncated) {
            return;
        }
        if (sizeBytes > limitBytes) {
            truncated = true;
            releaseContent();
            return;
        }
        buffer.write(content, offset, length);
    }

    void markComplete() {
        complete = true;
    }

    void markTruncated() {
        truncated = true;
        releaseContent();
    }

    void skip(String reason) {
        skipReason = reason;
        releaseContent();
    }

    void releaseContent() {
        buffer = null;
    }

    InputStream openStream() {
        return buffer != null ? buffer.openStream() : InputStream.nullInputStream();
    }

    String contentAsString(Charset charset) {
        return buffer != null ? buffer.contentAsString(charset) : "";
    }

    boolean isComplete() {
        return complete || expectedSize >= 0 && sizeBytes >= expectedSize;
    }

    boolean isTruncated() {
        return truncated;
    }

    boolean isSkipped() {
        return skipReason != null;
    }

    String getSkipReason() {
        return skipReason;
    }

    long getSizeBytes() {
        return sizeBytes;
    }

    int getLimitBytes() {
        return limitBytes;
    }

    private static final class CaptureBuffer extends ByteArrayOutputStream {

        private CaptureBuffer(int initialSize) {
            super(initialSize);
        }

        private InputStream openStream() {
            return new ByteArrayInputStream(buf, 0, count);
        }

        private String contentAsString(Charset charset) {
            return new String(buf, 0, count, charset);
        }
    }
}
