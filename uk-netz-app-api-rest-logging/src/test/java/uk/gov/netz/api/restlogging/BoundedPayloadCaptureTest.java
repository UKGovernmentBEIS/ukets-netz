package uk.gov.netz.api.restlogging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundedPayloadCaptureTest {

    @Test
    void requestCapturesValidJsonAtConfiguredDefaultBoundary() throws IOException {
        byte[] body = jsonAtSize(RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);

        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(
                request, RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        wrapper.getInputStream().readAllBytes();

        assertThat(body).hasSize(RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        assertThat(wrapper.getPayloadCapture().isComplete()).isTrue();
        assertThat(wrapper.getPayloadCapture().isTruncated()).isFalse();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(body);
    }

    @Test
    void requestDiscardsValidJsonOneByteAboveConfiguredBoundary() throws IOException {
        byte[] body = jsonAtSize(RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES + 1);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);

        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(
                request, RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        wrapper.getInputStream().readAllBytes();

        assertThat(wrapper.getPayloadCapture().isTruncated()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(body.length);
    }

    @Test
    void requestPassesThroughAndCapturesCompleteJsonAtLimit() throws IOException {
        byte[] body = "{\"value\":\"123456\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);

        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, body.length);

        assertThat(wrapper.getInputStream().readAllBytes()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().isComplete()).isTrue();
        assertThat(wrapper.getPayloadCapture().isTruncated()).isFalse();
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(body.length);
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(body);
    }

    @Test
    void requestPassesThroughButDoesNotRetainJsonAboveLimit() throws IOException {
        byte[] body = "{\"value\":\"1234567\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);

        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, body.length - 1);

        assertThat(wrapper.getInputStream().readAllBytes()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().isTruncated()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(body.length);
    }

    @Test
    void requestCompletesKnownLengthCaptureInTheExistingBuffer() throws IOException {
        byte[] body = "{\"value\":\"complete-after-error\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        assertThat(wrapper.getInputStream().readNBytes(8)).hasSize(8);
        assertThat(wrapper.getPayloadCapture().isComplete()).isFalse();

        wrapper.completeCapture();

        assertThat(wrapper.getPayloadCapture().isComplete()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(body);
    }

    @Test
    void requestCompletionWorksAfterDownstreamUsesReader() throws IOException {
        byte[] body = "{\"value\":\"reader-access\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent(body);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        assertThat(wrapper.getReader().read()).isEqualTo('{');
        wrapper.completeCapture();

        assertThat(wrapper.getPayloadCapture().isComplete()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(body);
    }

    @Test
    void requestReaderReportsInvalidCharsetAsUnsupportedEncoding() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getCharacterEncoding() {
                return "invalid-charset";
            }
        };
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        assertThatThrownBy(wrapper::getReader)
                .isInstanceOf(UnsupportedEncodingException.class)
                .hasMessage("invalid-charset");
    }

    @Test
    void requestDoesNotCompleteUnknownLengthCapture() throws IOException {
        byte[] body = "{\"value\":\"unknown-length\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        wrapper.getInputStream().readNBytes(8);
        wrapper.completeCapture();

        assertThat(wrapper.getPayloadCapture().isComplete()).isFalse();
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(8);
    }

    @Test
    void requestMarksCaptureSkippedWhenCompletionFails() throws IOException {
        byte[] body = "{\"value\":\"read-error\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public long getContentLengthLong() {
                return body.length;
            }

            @Override
            public ServletInputStream getInputStream() {
                return new FailingServletInputStream(body, 8);
            }
        };
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        wrapper.getInputStream().readNBytes(4);
        wrapper.completeCapture();

        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("READ_ERROR");
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
    }

    @Test
    void multipartCaptureRemainsSkippedUntilApplicationReadsJsonPart() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.addPart(new MockPart("metadata", "metadata.json", "{\"value\":1}".getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_JSON));
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        wrapper.getParts();

        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("MULTIPART");
    }

    @Test
    void multipartCapturesOnlyFirstJsonPartAsApplicationReadsIt() throws Exception {
        byte[] firstJson = "{\"value\":\"first\"}".getBytes(StandardCharsets.UTF_8);
        MockPart file = new MockPart("file", "file.bin", "file-content".getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_OCTET_STREAM);
        MockPart metadata = new MockPart("metadata", "metadata.json", firstJson, MediaType.APPLICATION_JSON);
        MockPart secondJson = new MockPart("other", "other.json", "{\"value\":\"second\"}"
                .getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.addPart(file);
        request.addPart(metadata);
        request.addPart(secondJson);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        Collection<Part> parts = wrapper.getParts();
        Part wrappedMetadata = parts.stream().filter(part -> "metadata".equals(part.getName())).findFirst().orElseThrow();
        Part unwrappedFile = parts.stream().filter(part -> "file".equals(part.getName())).findFirst().orElseThrow();
        wrappedMetadata.getInputStream().readAllBytes();
        parts.stream().filter(part -> "other".equals(part.getName())).findFirst().orElseThrow()
                .getInputStream().readAllBytes();

        assertThat(unwrappedFile).isSameAs(file);
        assertThat(wrappedMetadata).isNotSameAs(metadata);
        assertThat(wrapper.getPayloadCapture().isComplete()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(firstJson);
    }

    @Test
    void multipartDirectPartAccessDoesNotDuplicateCaptureAcrossStreams() throws Exception {
        byte[] json = "{\"value\":\"metadata\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.addPart(new MockPart("metadata", "metadata.json", json, MediaType.APPLICATION_JSON));
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);
        Part metadata = wrapper.getPart("metadata");

        metadata.getInputStream().readAllBytes();
        metadata.getInputStream().readAllBytes();

        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(json.length);
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(json);
    }

    @Test
    void multipartJsonPartAboveLimitIsObservedButNotRetained() throws Exception {
        byte[] json = "{\"value\":\"too-large\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.addPart(new MockPart("metadata", "metadata.json", json, MediaType.APPLICATION_JSON));
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, json.length - 1);

        wrapper.getPart("metadata").getInputStream().readAllBytes();

        assertThat(wrapper.getPayloadCapture().isTruncated()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(json.length);
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
    }

    @Test
    void multipartParsingFailurePropagatesWithoutChangingSkipMetadata() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public java.util.Collection<Part> getParts() throws jakarta.servlet.ServletException {
                throw new jakarta.servlet.ServletException("simulated multipart failure");
            }
        };
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        assertThatThrownBy(wrapper::getParts).isInstanceOf(jakarta.servlet.ServletException.class);
        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("MULTIPART");
    }

    @Test
    void multipartJsonPartReadFailureIsPropagatedAndRecorded() throws Exception {
        Part metadata = Mockito.mock(Part.class);
        Mockito.when(metadata.getName()).thenReturn("metadata");
        Mockito.when(metadata.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        Mockito.when(metadata.getSize()).thenReturn(32L);
        Mockito.when(metadata.getInputStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("simulated part read failure");
            }
        });
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.addPart(metadata);
        BoundedRequestCaptureWrapper wrapper = new BoundedRequestCaptureWrapper(request, 1024);

        Part wrappedMetadata = wrapper.getPart("metadata");

        try (InputStream inputStream = wrappedMetadata.getInputStream()) {
            assertThatThrownBy(inputStream::readAllBytes)
                    .isInstanceOf(IOException.class);
        }
        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("READ_ERROR");
    }

    @Test
    void responseStreamsTwentyMegabytesWithoutRetainingAnOverflowPrefix() throws IOException {
        byte[] body = new byte[20 * 1024 * 1024];
        java.util.Arrays.fill(body, (byte) 'x');
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        BoundedContentCachingResponseWrapper wrapper = new BoundedContentCachingResponseWrapper(
                response, RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);

        wrapper.getOutputStream().write(body);
        wrapper.finishCapture();

        assertThat(response.getContentAsByteArray()).hasSize(body.length);
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(body.length);
        assertThat(wrapper.getPayloadCapture().isTruncated()).isTrue();
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
    }

    @Test
    void responseWriterCapturesEncodedJsonAndForwardsIt() throws IOException {
        String body = "{\"value\":\"hello\"}";
        MockHttpServletResponse response = new MockHttpServletResponse();
        BoundedContentCachingResponseWrapper wrapper = new BoundedContentCachingResponseWrapper(response, 1024);
        wrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
        wrapper.setCharacterEncoding(StandardCharsets.UTF_8.name());

        wrapper.getWriter().write(body);
        wrapper.finishCapture();

        assertThat(response.getContentAsString()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes())
                .isEqualTo(body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void responseWriterReportsInvalidCharsetAsUnsupportedEncoding() {
        MockHttpServletResponse response = new MockHttpServletResponse() {
            @Override
            public String getCharacterEncoding() {
                return "invalid-charset";
            }
        };
        BoundedContentCachingResponseWrapper wrapper =
                new BoundedContentCachingResponseWrapper(response, 1024);

        assertThatThrownBy(wrapper::getWriter)
                .isInstanceOf(UnsupportedEncodingException.class)
                .hasMessage("invalid-charset");
    }

    @Test
    void nonJsonResponseIsCountedButNotRetained() throws IOException {
        byte[] body = "file-content".getBytes(StandardCharsets.UTF_8);
        MockHttpServletResponse response = new MockHttpServletResponse();
        BoundedContentCachingResponseWrapper wrapper = new BoundedContentCachingResponseWrapper(response, 1024);
        wrapper.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        wrapper.getOutputStream().write(body);
        wrapper.finishCapture();

        assertThat(response.getContentAsByteArray()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("NON_JSON_CONTENT_TYPE");
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEmpty();
    }

    @Test
    void responseSetHeaderContentTypeAppliesCapturePolicy() throws IOException {
        byte[] body = "plain-text".getBytes(StandardCharsets.UTF_8);
        MockHttpServletResponse response = responseWithoutContentTypeMetadata();
        BoundedContentCachingResponseWrapper wrapper =
                new BoundedContentCachingResponseWrapper(response, 1024);
        wrapper.setHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE);

        wrapper.getOutputStream().write(body);
        wrapper.finishCapture();

        assertThat(response.getContentAsByteArray()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("NON_JSON_CONTENT_TYPE");
    }

    @Test
    void responseAddHeaderContentTypeAppliesCapturePolicy() throws IOException {
        byte[] body = "plain-text".getBytes(StandardCharsets.UTF_8);
        MockHttpServletResponse response = responseWithoutContentTypeMetadata();
        BoundedContentCachingResponseWrapper wrapper =
                new BoundedContentCachingResponseWrapper(response, 1024);
        wrapper.addHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE);

        wrapper.getOutputStream().write(body);
        wrapper.finishCapture();

        assertThat(response.getContentAsByteArray()).isEqualTo(body);
        assertThat(wrapper.getPayloadCapture().isSkipped()).isTrue();
        assertThat(wrapper.getPayloadCapture().getSkipReason()).isEqualTo("NON_JSON_CONTENT_TYPE");
    }

    @Test
    void resetBufferKeepsExistingOutputStreamAttachedToFreshCapture() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        BoundedContentCachingResponseWrapper wrapper = new BoundedContentCachingResponseWrapper(response, 1024);
        wrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ServletOutputStream outputStream = wrapper.getOutputStream();
        outputStream.write("{\"discarded\":true}".getBytes(StandardCharsets.UTF_8));

        wrapper.resetBuffer();
        byte[] retained = "{\"retained\":true}".getBytes(StandardCharsets.UTF_8);
        outputStream.write(retained);
        wrapper.finishCapture();

        assertThat(response.getContentAsByteArray()).isEqualTo(retained);
        assertThat(wrapper.getPayloadCapture().getSizeBytes()).isEqualTo(retained.length);
        assertThat(wrapper.getPayloadCapture().openStream().readAllBytes()).isEqualTo(retained);
    }

    private static byte[] jsonAtSize(int sizeBytes) {
        int envelopeBytes = "{\"value\":\"\"}".getBytes(StandardCharsets.UTF_8).length;
        return ("{\"value\":\"" + "x".repeat(sizeBytes - envelopeBytes) + "\"}")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static MockHttpServletResponse responseWithoutContentTypeMetadata() {
        return new MockHttpServletResponse() {
            @Override
            public String getContentType() {
                return null;
            }
        };
    }

    private static final class FailingServletInputStream extends ServletInputStream {

        private final byte[] content;
        private final int failAtIndex;
        private int index;

        private FailingServletInputStream(byte[] content, int failAtIndex) {
            this.content = content;
            this.failAtIndex = failAtIndex;
        }

        @Override
        public int read() throws IOException {
            if (index >= failAtIndex) {
                throw new IOException("simulated read failure");
            }
            return index < content.length ? content[index++] & 0xff : -1;
        }

        @Override
        public boolean isFinished() {
            return index >= content.length;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
