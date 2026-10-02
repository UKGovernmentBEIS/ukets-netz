package uk.gov.netz.api.notification.mail.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PublicFileUrlFactoryTest {

    private static final String ORIGIN = "https://files.example.gov.uk";
    private static final UUID FILE_UUID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    @ParameterizedTest
    @CsvSource({
        "'', ''",
        "/, ''",
        "////, ''",
        "/assets, /assets",
        "/assets/, /assets",
        "/assets///, /assets",
        "/assets/images///, /assets/images",
        "/assets//images///, /assets//images",
        "/assets///images, /assets///images",
        "/my%20files///, /my%20files",
        "/base%2Fsegment///, /base%2Fsegment",
        "/base%2fsegment///, /base%2fsegment",
        "/base%2F///, /base%2F",
        "/base%252Fsegment///, /base%252Fsegment",
        "/base%25segment///, /base%25segment",
        "/base%3Fsegment%23section///, /base%3Fsegment%23section",
        "/assets///%0A%0A, /assets///%0A%0A",
        "/assets///%0D%0D%0A, /assets///%0D%0D%0A",
        "/assets///%0A/images///, /assets///%0A/images",
        "/assets///%0A///, /assets///%0A",
        "/assets///%0B, /assets///%0B",
        "/assets///%0C, /assets///%0C"
    })
    void joinsBasePathAndStoredPath(String basePath, String expectedBasePath) {
        PublicFileUrlFactory factory = new PublicFileUrlFactory(URI.create(ORIGIN + basePath));

        assertThat(factory.create("application/email/" + FILE_UUID))
                .isEqualTo(URI.create(ORIGIN + expectedBasePath + "/application/email/" + FILE_UUID));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%0A", "%0D", "%0D%0A", "%C2%85", "%E2%80%A8", "%E2%80%A9"})
    void preservesSlashesBeforeFinalEncodedLineTerminator(String terminator) {
        PublicFileUrlFactory factory = new PublicFileUrlFactory(
                URI.create(ORIGIN + "/assets///" + terminator));

        assertThat(factory.create("application/" + FILE_UUID))
                .isEqualTo(URI.create(ORIGIN + "/assets///" + terminator + "/application/" + FILE_UUID));
    }

    @ParameterizedTest
    @CsvSource({
        "my files/report.txt, my%20files/report.txt",
        "reports/literal%2F.txt, reports/literal%252F.txt",
        "reports/100%.txt, reports/100%25.txt",
        "reports/what?#.txt, reports/what%3F%23.txt",
        "reports/a&b+file;v=1.txt, reports/a&b+file;v=1.txt",
        "/exact//path/, /exact//path/"
    })
    void encodesStoredPathOnceWithoutChangingBasePath(String storedPath, String expectedPath) {
        PublicFileUrlFactory factory = new PublicFileUrlFactory(URI.create(ORIGIN + "/base%2Fsegment///"));

        URI url = factory.create(storedPath);

        assertThat(url).isEqualTo(URI.create(ORIGIN + "/base%2Fsegment/" + expectedPath));
        assertThat(url.getQuery()).isNull();
        assertThat(url.getFragment()).isNull();
    }

    @Test
    void preservesExplicitPortAndNormalizesScheme() {
        PublicFileUrlFactory factory = new PublicFileUrlFactory(
                URI.create("HTTP://localhost:4566/assets///"));

        assertThat(factory.create("application/" + FILE_UUID).toString())
                .isEqualTo("http://localhost:4566/assets/application/" + FILE_UUID);
    }

    @Test
    void preservesLongSlashSequenceFollowedByNonSlashCharacter() {
        String basePath = "/assets/" + "/".repeat(16_000) + "image";
        PublicFileUrlFactory factory = new PublicFileUrlFactory(URI.create(ORIGIN + basePath));

        assertThat(factory.create("application/" + FILE_UUID))
                .isEqualTo(URI.create(ORIGIN + basePath + "/application/" + FILE_UUID));
    }

    @Test
    void removesLongTrailingSlashSequence() {
        PublicFileUrlFactory factory = new PublicFileUrlFactory(
                URI.create(ORIGIN + "/assets" + "/".repeat(16_000)));

        assertThat(factory.create("application/" + FILE_UUID))
                .isEqualTo(URI.create(ORIGIN + "/assets/application/" + FILE_UUID));
    }
}
