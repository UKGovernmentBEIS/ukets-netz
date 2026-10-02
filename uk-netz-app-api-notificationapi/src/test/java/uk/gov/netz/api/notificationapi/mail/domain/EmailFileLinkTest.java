package uk.gov.netz.api.notificationapi.mail.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailFileLinkTest {

    @Test
    void preservesValuesAndProvidesValueEquality() {
        UUID uuid = UUID.fromString("26e481e4-184b-43b3-8956-0d006b7fd943");
        URI url = URI.create("https://files.example.test/26e481e4-184b-43b3-8956-0d006b7fd943");

        EmailFileLink link = EmailFileLink.builder()
            .uuid(uuid)
            .fileName("report.pdf")
            .url(url)
            .build();
        EmailFileLink equalLink = EmailFileLink.builder()
            .uuid(uuid)
            .fileName("report.pdf")
            .url(url)
            .build();

        assertThat(link.getUuid()).isEqualTo(uuid);
        assertThat(link.getFileName()).isEqualTo("report.pdf");
        assertThat(link.getUrl()).isEqualTo(url);
        assertThat(link).isEqualTo(equalLink).hasSameHashCodeAs(equalLink);
    }

}
