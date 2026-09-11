package uk.gov.netz.api.restlogging.itapp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.netz.api.restlogging.RestLoggingConfig;
import uk.gov.netz.api.restlogging.RestLoggingFilter;
import uk.gov.netz.api.restlogging.RestLoggingService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootApplication
@Import({RestLoggingConfig.class, RestLoggingFilter.class, RestLoggingService.class})
public class RestLoggingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestLoggingTestApplication.class, args);
    }

    @RestController
    @RequestMapping("/it")
    static class TestController {

        private static final int LARGE_BODY_BYTES = 20 * 1024 * 1024;
        private static final byte[] LARGE_RESPONSE_PREFIX =
                "{\"password\":\"large-response-secret\",\"data\":\"".getBytes(StandardCharsets.UTF_8);
        private static final byte[] JSON_SUFFIX = "\"}".getBytes(StandardCharsets.UTF_8);

        @GetMapping("/health")
        TestResponse health() {
            return new TestResponse("up", "health-secret");
        }

        @PostMapping(path = "/small", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        TestResponse small(@RequestBody TestRequest request) {
            return new TestResponse(request.message(), "small-response-secret");
        }

        @PostMapping(path = "/large", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        void large(HttpServletRequest request, HttpServletResponse response) throws IOException {
            long consumedBytes = request.getInputStream().transferTo(OutputStream.nullOutputStream());
            response.setHeader("X-Consumed-Bytes", Long.toString(consumedBytes));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setContentLengthLong(LARGE_BODY_BYTES);

            OutputStream output = response.getOutputStream();
            output.write(LARGE_RESPONSE_PREFIX);
            writeRepeated(output, LARGE_BODY_BYTES - LARGE_RESPONSE_PREFIX.length - JSON_SUFFIX.length, (byte) 'r');
            output.write(JSON_SUFFIX);
        }

        @PostMapping(path = "/partial-error", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        void partialError(HttpServletRequest request, HttpServletResponse response) throws IOException {
            byte[] consumed = request.getInputStream().readNBytes(256);
            response.setHeader("X-Consumed-Bytes", Integer.toString(consumed.length));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getOutputStream().write("{\"error\":\"invalid request\"}".getBytes(StandardCharsets.UTF_8));
        }

        @PostMapping(path = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        TestResponse multipart(@RequestPart("metadata") TestRequest metadata,
                @RequestPart("file") MultipartFile file) {
            return new TestResponse(metadata.message() + ":" + file.getSize(), "multipart-response-secret");
        }

        @PostMapping(path = "/multipart-ignored", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
        TestResponse multipartIgnored() {
            return new TestResponse("ignored", "multipart-ignored-response-secret");
        }

        private static void writeRepeated(OutputStream output, int count, byte value) throws IOException {
            byte[] buffer = new byte[8192];
            Arrays.fill(buffer, value);
            int remaining = count;
            while (remaining > 0) {
                int written = Math.min(remaining, buffer.length);
                output.write(buffer, 0, written);
                remaining -= written;
            }
        }
    }

    record TestRequest(String message, String password) {
    }

    record TestResponse(String result, String password) {
    }
}
