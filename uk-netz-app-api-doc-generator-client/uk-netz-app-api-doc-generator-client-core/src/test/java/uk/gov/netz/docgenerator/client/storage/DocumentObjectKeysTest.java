package uk.gov.netz.docgenerator.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DocumentObjectKeysTest {

    @Test
    void buildsCanonicalObjectKeys() {
        assertThat(DocumentObjectKeys.inputDocx("job-1")).isEqualTo("input/job-1.docx");
        assertThat(DocumentObjectKeys.statusPrefix("job-1")).isEqualTo("status/job-1/");
        assertThat(DocumentObjectKeys.statusMarker("job-1", StatusMarker.UPLOADED)).isEqualTo("status/job-1/uploaded");
        assertThat(DocumentObjectKeys.statusMarker("job-1", StatusMarker.SUBMITTED)).isEqualTo("status/job-1/submitted");
        assertThat(DocumentObjectKeys.statusMarker("job-1", StatusMarker.SUBMISSION_FAILED)).isEqualTo("status/job-1/submission_failed");
        assertThat(DocumentObjectKeys.statusMarker("job-1", StatusMarker.PROCESSING)).isEqualTo("status/job-1/processing");
        assertThat(DocumentObjectKeys.outputPrefix("job-1")).isEqualTo("output/job-1");
        assertThat(DocumentObjectKeys.outputPdf("job-1")).isEqualTo("output/job-1.pdf");
        assertThat(DocumentObjectKeys.errorJson("job-1")).isEqualTo("output/job-1.error.json");
    }

    @Test
    void parsesKnownStatusMarkersOnlyForTheRequestedJob() {
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/uploaded")).contains(StatusMarker.UPLOADED);
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/submitted")).contains(StatusMarker.SUBMITTED);
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/submission_failed")).contains(StatusMarker.SUBMISSION_FAILED);
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/processing")).contains(StatusMarker.PROCESSING);

        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-2/uploaded")).isEmpty();
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/unknown")).isEmpty();
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/")).isEmpty();
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", "status/job-1/uploaded/extra")).isEmpty();
        assertThat(DocumentObjectKeys.parseStatusMarker("job-1", null)).isEmpty();
    }

    @Test
    void rejectsBlankJobIds() {
        assertThatThrownBy(() -> DocumentObjectKeys.inputDocx(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("jobId must not be blank");
    }
}
