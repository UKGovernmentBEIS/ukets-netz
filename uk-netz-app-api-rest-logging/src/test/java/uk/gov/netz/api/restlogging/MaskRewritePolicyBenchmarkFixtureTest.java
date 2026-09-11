package uk.gov.netz.api.restlogging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskRewritePolicyBenchmarkFixtureTest {

    @Test
    void benchmarkFixtureExercisesStructuredAndTextMaskingPaths() throws Exception {
        MaskRewritePolicyBenchmark benchmark = new MaskRewritePolicyBenchmark();
        benchmark.setUpTrial();

        assertThat(benchmark.rewriteStructuredRestEntry().getMessage().getFormattedMessage())
                .contains("password=[REDACTED]")
                .doesNotContain("ssssssss");
        assertThat(benchmark.rewriteJsonTextFallback().getMessage().getFormattedMessage())
                .contains("\"password\":\"[REDACTED]\"")
                .doesNotContain("ssssssss");
    }
}
