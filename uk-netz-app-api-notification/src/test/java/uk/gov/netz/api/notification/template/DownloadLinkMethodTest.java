package uk.gov.netz.api.notification.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateRepository;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.DOWNLOAD_LINK;

@ExtendWith(MockitoExtension.class)
class DownloadLinkMethodTest {

    @Mock
    private NotificationTemplateRepository repository;
    private NotificationTemplateProcessService renderer;

    @BeforeEach
    void setUp() {
        renderer = new NotificationTemplateProcessService(new CustomFreeMarkerConfiguration().freemarkerConfig(), repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"report [draft](v2).pdf", "[invoice](https://evil.test) ![image](x).pdf",
        "report_*draft*`code`\\copy.pdf", "αρχείο_日本語.pdf"})
    void filenamesRemainLiteralLinkLabels(String name) {
        String body = render("${downloadLink(file)}", name, "https://files.example.test/id");

        assertThat(body).contains(name).containsOnlyOnce("<a ").contains("href=\"https://files.example.test/id\"")
                .doesNotContain("<img", "<em>", "<strong>", "<code>", "href=\"https://evil.test\"");
    }

    @Test
    void labelsAreHtmlEscapedWithoutDoubleEscapingEntities() {
        String body = render("${downloadLink(file)}", "report [draft] & <review>.pdf", "https://files.example.test/id");

        assertThat(body).contains("report [draft] &amp; &lt;review&gt;.pdf").doesNotContain("<review>", "&amp;amp;");
    }

    @ParameterizedTest
    @CsvSource({
        "https://files.example.test/base%2Fsegment/a(b)?x=1&y=2, https://files.example.test/base%2Fsegment/a%28b%29?x=1&amp;y=2",
        "https://files.example.test/my%20files/100%25, https://files.example.test/my%20files/100%25",
        "https://files.example.test/report%252Fname, https://files.example.test/report%252Fname"
    })
    void urlDelimitersAndExistingPercentEscapesSurviveRendering(String url, String expectedHref) {
        assertThat(render("${downloadLink(file)}", "report.pdf", url))
                .contains("href=\"" + expectedHref + "\">report.pdf</a>");
    }

    @Test
    void templateCanSupplyItsOwnLabel() {
        assertThat(render("${downloadLink(file, 'Download [this] & review')}", "report.pdf", "https://files.example.test/id"))
                .contains(">Download [this] &amp; review</a>").doesNotContain("report.pdf");
    }

    @ParameterizedTest
    @ValueSource(strings = {"${downloadLink()}", "${downloadLink('not a file')}", "${downloadLink(file, 42)}",
        "${downloadLink(file, 'label', 'extra')}"})
    void invalidHelperArgumentsFailTemplateProcessing(String text) {
        assertThatThrownBy(() -> render(text, "report.pdf", "https://files.example.test/id"))
                .isInstanceOf(BusinessException.class);
    }

    private String render(String text, String name, String url) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName("test");
        template.setSubject("Subject");
        template.setText(text);
        when(repository.findByNameAndCompetentAuthority("test", null)).thenReturn(Optional.of(template));
        EmailFileLink file = EmailFileLink.builder().uuid(UUID.randomUUID()).fileName(name).url(URI.create(url)).build();
        return renderer.processEmailNotificationTemplate("test", null,
                Map.of("file", file, DOWNLOAD_LINK, new DownloadLinkMethod())).getText();
    }
}
