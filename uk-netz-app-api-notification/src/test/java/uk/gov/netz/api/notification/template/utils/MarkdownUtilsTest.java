package uk.gov.netz.api.notification.template.utils;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.notification.template.MarkdownUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownUtilsTest {

	@Test
	void parseToHtml() {
		String markdownText = "Hello *NETZ* in bold";
		String htmlText = MarkdownUtils.parseToHtml(markdownText);
		String expectedHtmlText = "<p>Hello <em>NETZ</em> in bold</p>\n";
		assertThat(htmlText).isEqualTo(expectedHtmlText);
	}

	@Test
	void parseToHtml_escape_html_tags() {
		String markdownText = "<div>text</div>";
		String htmlText = MarkdownUtils.parseToHtml(markdownText);
		String expectedHtmlText = "<p>&lt;div&gt;text&lt;/div&gt;</p>\n";
		assertThat(htmlText).isEqualTo(expectedHtmlText);
	}
}
