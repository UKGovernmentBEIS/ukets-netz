package uk.gov.netz.api.restlogging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Pass-through request wrapper that retains a bounded copy for logging while the downstream application consumes
 * the original one-shot request stream. It does not make the request body replayable.
 */
final class BoundedRequestCaptureWrapper extends HttpServletRequestWrapper {

    private final PayloadCapture payloadCapture;
    private final boolean multipart;
    private final Map<Part, Part> wrappedParts = new IdentityHashMap<>();
    private ServletInputStream inputStream;
    private BufferedReader reader;
    private Part selectedJsonPart;
    private boolean multipartCaptureClaimed;

    BoundedRequestCaptureWrapper(HttpServletRequest request, int limitBytes) {
        super(request);
        payloadCapture = new PayloadCapture(limitBytes, request.getContentLengthLong());
        multipart = RestLoggingUtils.isMultipart(request.getContentType());
        if (multipart) {
            payloadCapture.skip("MULTIPART");
        } else if (request.getContentType() != null && !RestLoggingUtils.isJsonContentType(request.getContentType())) {
            payloadCapture.skip("NON_JSON_CONTENT_TYPE");
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new CapturingServletInputStream(super.getInputStream(), payloadCapture);
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            Charset charset = ServletCharsetResolver.resolve(getCharacterEncoding());
            reader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
        return reader;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        Collection<Part> parts = super.getParts();
        if (!multipart) {
            return parts;
        }
        Collection<Part> wrapped = new ArrayList<>(parts.size());
        for (Part part : parts) {
            wrapped.add(wrapJsonPart(part));
        }
        return wrapped;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        Part part = super.getPart(name);
        return multipart ? wrapJsonPart(part) : part;
    }

    PayloadCapture getPayloadCapture() {
        return payloadCapture;
    }

    void completeCapture() {
        long contentLength = getContentLengthLong();
        if (contentLength < 0
                || contentLength > payloadCapture.getLimitBytes()
                || payloadCapture.isComplete()
                || payloadCapture.isSkipped()
                || payloadCapture.isTruncated()) {
            return;
        }

        byte[] buffer = new byte[8192];
        try {
            ServletInputStream stream = getInputStream();
            while (payloadCapture.getSizeBytes() < contentLength) {
                int remaining = (int) Math.min(buffer.length, contentLength - payloadCapture.getSizeBytes());
                int read = stream.read(buffer, 0, remaining);
                if (read == -1) {
                    break;
                }
            }
            if (payloadCapture.getSizeBytes() < contentLength) {
                payloadCapture.skip("READ_ERROR");
            } else {
                payloadCapture.markComplete();
            }
        } catch (IOException | RuntimeException ex) {
            payloadCapture.skip("READ_ERROR");
        }
    }

    private synchronized Part wrapJsonPart(Part part) {
        if (part == null || !RestLoggingUtils.isJsonContentType(part.getContentType())) {
            return part;
        }
        if (selectedJsonPart == null) {
            selectedJsonPart = part;
        }
        if (selectedJsonPart != part) {
            return part;
        }
        return wrappedParts.computeIfAbsent(part, CapturingPart::new);
    }

    private synchronized boolean claimMultipartCapture(Part part) {
        if (multipartCaptureClaimed) {
            return false;
        }
        multipartCaptureClaimed = true;
        payloadCapture.reset(part.getSize());
        return true;
    }

    private final class CapturingPart implements Part {

        private final Part delegate;

        private CapturingPart(Part delegate) {
            this.delegate = delegate;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new CapturingPartInputStream(delegate.getInputStream(), delegate);
        }

        @Override
        public String getContentType() {
            return delegate.getContentType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getSubmittedFileName() {
            return delegate.getSubmittedFileName();
        }

        @Override
        public long getSize() {
            return delegate.getSize();
        }

        @Override
        public void write(String fileName) throws IOException {
            delegate.write(fileName);
        }

        @Override
        public void delete() throws IOException {
            delegate.delete();
        }

        @Override
        public String getHeader(String name) {
            return delegate.getHeader(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return delegate.getHeaders(name);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return delegate.getHeaderNames();
        }
    }

    private final class CapturingPartInputStream extends FilterInputStream {

        private final Part part;
        private Boolean captureOwner;

        private CapturingPartInputStream(InputStream delegate, Part part) {
            super(delegate);
            this.part = part;
        }

        @Override
        public int read() throws IOException {
            boolean owner = isCaptureOwner();
            try {
                int value = super.read();
                capture(owner, value, null, 0, 0);
                return value;
            } catch (IOException ex) {
                captureFailed(owner);
                throw ex;
            }
        }

        @Override
        public int read(byte[] content, int offset, int length) throws IOException {
            boolean owner = isCaptureOwner();
            try {
                int read = super.read(content, offset, length);
                capture(owner, read, content, offset, read);
                return read;
            } catch (IOException ex) {
                captureFailed(owner);
                throw ex;
            }
        }

        @Override
        public long skip(long count) throws IOException {
            if (count <= 0) {
                return 0;
            }
            if (!isCaptureOwner()) {
                return super.skip(count);
            }
            long skipped = 0;
            byte[] buffer = new byte[(int) Math.min(8192, count)];
            while (skipped < count) {
                int read = read(buffer, 0, (int) Math.min(buffer.length, count - skipped));
                if (read == -1) {
                    break;
                }
                skipped += read;
            }
            return skipped;
        }

        private boolean isCaptureOwner() {
            if (captureOwner == null) {
                captureOwner = claimMultipartCapture(part);
            }
            return captureOwner;
        }

        private void capture(boolean owner, int value, byte[] content, int offset, int length) {
            if (!owner) {
                return;
            }
            if (value == -1) {
                payloadCapture.markComplete();
            } else if (content == null) {
                payloadCapture.accept(value);
            } else {
                payloadCapture.accept(content, offset, length);
            }
        }

        private void captureFailed(boolean owner) {
            if (owner) {
                payloadCapture.skip("READ_ERROR");
            }
        }
    }

    private static final class CapturingServletInputStream extends ServletInputStream {

        private final ServletInputStream delegate;
        private final PayloadCapture payloadCapture;

        private CapturingServletInputStream(ServletInputStream delegate, PayloadCapture payloadCapture) {
            this.delegate = delegate;
            this.payloadCapture = payloadCapture;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value == -1) {
                payloadCapture.markComplete();
            } else {
                payloadCapture.accept(value);
            }
            return value;
        }

        @Override
        public int read(byte[] content, int offset, int length) throws IOException {
            int read = delegate.read(content, offset, length);
            if (read == -1) {
                payloadCapture.markComplete();
            } else {
                payloadCapture.accept(content, offset, read);
            }
            return read;
        }

        @Override
        public boolean isFinished() {
            boolean finished = delegate.isFinished();
            if (finished) {
                payloadCapture.markComplete();
            }
            return finished;
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            delegate.setReadListener(listener);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            if (delegate.isFinished()) {
                payloadCapture.markComplete();
            }
        }
    }
}
