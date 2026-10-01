package uk.gov.netz.api.files.common.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileNameSanitizerTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "notice.pdf", "report", "report Q1 (final).pdf", "O'Brien.pdf", "archive.tar.gz",
        ".notice.pdf", "résumé.pdf", "船舶-🚢.pdf", "report\u2003Q1.pdf"
    })
    void preservesValidFileNames(String fileName) {
        assertThat(FileNameSanitizer.sanitize(fileName)).isEqualTo(fileName);
    }

    @ParameterizedTest
    @MethodSource("fileNamesToSanitize")
    void stripsPathsAndUnsafeCharacters(String fileName, String expected) {
        assertThat(FileNameSanitizer.sanitize(fileName)).isEqualTo(expected);
    }

    private static Stream<Arguments> fileNamesToSanitize() {
        return Stream.of(
                Arguments.of("folder/notice.pdf", "notice.pdf"),
                Arguments.of("/tmp/notices/notice.pdf", "notice.pdf"),
                Arguments.of("C:\\tmp\\notice.pdf", "notice.pdf"),
                Arguments.of("C:\\tmp/reports\\notice.pdf", "notice.pdf"),
                Arguments.of("../../notice.pdf", "notice.pdf"),
                Arguments.of("\"notice\".pdf", "notice.pdf"),
                Arguments.of("not;ice.pdf", "notice.pdf"),
                Arguments.of("not\0ice.pdf", "notice.pdf"),
                Arguments.of("not\tice.pdf", "notice.pdf"),
                Arguments.of("not\r\nice.pdf", "notice.pdf"),
                Arguments.of("not\u007fice.pdf", "notice.pdf"),
                Arguments.of("not\u0085ice.pdf", "notice.pdf"),
                Arguments.of("  notice.pdf  ", "notice.pdf"),
                Arguments.of("  report Q1.pdf  ", "report Q1.pdf"),
                Arguments.of("\u2003report.pdf\u2003", "report.pdf"),
                Arguments.of("\u2002\u3000résumé.pdf\u3000\u2002", "résumé.pdf"),
                Arguments.of("folder/\u2003 \"not;ice\r\n.pdf\" \u2003", "notice.pdf"),
                Arguments.of("C:\\tmp\\ \"not;ice\r\n.pdf\"  ", "notice.pdf")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "\t\r\n", "\u2003", ".", "..", "...", "  ...  ",
        "/", "\\", "folder/", "C:\\tmp\\", "../", "folder/...",
        "\0", "\r\n\u007f\u0085", "\"", ";", "\";", "\t\";\0",
        " .\";\r\n. ", "\u2003\";\u2003", "\u2003...\u2003", "folder/\u2003..\u2003",
        "\u2002\u3000.\u3000\u2002", "folder/\u2003\u3000"
    })
    void rejectsFileNamesWithoutAMeaningfulBaseName(String fileName) {
        assertThatThrownBy(() -> FileNameSanitizer.sanitize(fileName)).isInstanceOf(IllegalArgumentException.class);
    }
}
