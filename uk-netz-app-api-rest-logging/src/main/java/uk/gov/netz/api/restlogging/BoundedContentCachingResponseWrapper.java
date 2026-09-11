package uk.gov.netz.api.restlogging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

final class BoundedContentCachingResponseWrapper extends HttpServletResponseWrapper {

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_TYPE = "Content-Type";

    private final int limitBytes;
    private final PayloadCapture payloadCapture;
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    BoundedContentCachingResponseWrapper(HttpServletResponse response, int limitBytes) {
        super(response);
        this.limitBytes = limitBytes;
        this.payloadCapture = new PayloadCapture(limitBytes, resolveContentLength(response.getHeader(CONTENT_LENGTH)));
        applyCapturePolicy();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        if (outputStream == null) {
            outputStream = new CapturingServletOutputStream(super.getOutputStream(), payloadCapture);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        Charset charset = ServletCharsetResolver.resolve(getCharacterEncoding());
        outputStream = new CapturingServletOutputStream(super.getOutputStream(), payloadCapture);
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset));
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            outputStream.flush();
        }
        super.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        resetCapture();
    }

    @Override
    public void reset() {
        super.reset();
        resetCapture();
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        applyCapturePolicy();
    }

    @Override
    public void setContentLength(int length) {
        super.setContentLength(length);
        if (length > limitBytes) {
            payloadCapture.markTruncated();
        }
    }

    @Override
    public void setContentLengthLong(long length) {
        super.setContentLengthLong(length);
        if (length > limitBytes) {
            payloadCapture.markTruncated();
        }
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        applyHeaderPolicy(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        applyHeaderPolicy(name, value);
    }

    void finishCapture() {
        if (writer != null) {
            writer.flush();
        }
        payloadCapture.markComplete();
    }

    PayloadCapture getPayloadCapture() {
        return payloadCapture;
    }

    private void resetCapture() {
        payloadCapture.reset(-1);
        applyCapturePolicy();
    }

    private void applyHeaderPolicy(String name, String value) {
        if (CONTENT_DISPOSITION.equalsIgnoreCase(name) && value != null) {
            payloadCapture.skip("FILE_CONTENT");
        } else if (CONTENT_TYPE.equalsIgnoreCase(name)) {
            applyCapturePolicy(value);
        } else if (CONTENT_LENGTH.equalsIgnoreCase(name) && resolveContentLength(value) > limitBytes) {
            payloadCapture.markTruncated();
        }
    }

    private void applyCapturePolicy() {
        String contentType = getHeader(CONTENT_TYPE);
        applyCapturePolicy(contentType != null ? contentType : getContentType());
    }

    private void applyCapturePolicy(String contentType) {
        if (getHeader(CONTENT_DISPOSITION) != null) {
            payloadCapture.skip("FILE_CONTENT");
        } else if (contentType != null && !RestLoggingUtils.isJsonContentType(contentType)) {
            payloadCapture.skip("NON_JSON_CONTENT_TYPE");
        }
    }

    private static long resolveContentLength(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static final class CapturingServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream delegate;
        private final PayloadCapture payloadCapture;

        private CapturingServletOutputStream(ServletOutputStream delegate, PayloadCapture payloadCapture) {
            this.delegate = delegate;
            this.payloadCapture = payloadCapture;
        }

        @Override
        public void write(int value) throws IOException {
            delegate.write(value);
            payloadCapture.accept(value);
        }

        @Override
        public void write(byte[] content, int offset, int length) throws IOException {
            delegate.write(content, offset, length);
            payloadCapture.accept(content, offset, length);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            payloadCapture.markComplete();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            delegate.setWriteListener(listener);
        }
    }
}
